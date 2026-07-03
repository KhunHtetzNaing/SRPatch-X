package com.htetz.core

import com.android.apksig.util.DataSources
import com.android.tools.build.apkzlib.sign.SigningExtension
import com.android.tools.build.apkzlib.sign.SigningOptions
import com.android.tools.build.apkzlib.zip.AlignmentRule
import com.android.tools.build.apkzlib.zip.AlignmentRules
import com.android.tools.build.apkzlib.zip.ZFile
import com.android.tools.build.apkzlib.zip.ZFileOptions
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * APK Patcher - Modifies an existing APK by adding patches and native libraries
 */
class ApkPatcher(
    private val sourceApk: File,
    private val outputApk: File,
    private val options: ApkPatchOptions,
    private val onProgress: (String) -> Unit = {}
) {
    fun patch() {
        onProgress("Validating input files...")

        require(sourceApk.exists()) { "Source APK not found: ${sourceApk.absolutePath}" }

        onProgress("Extracting APK signature...")
        val apkSignature = extractSignature()
        onProgress("Signature: $apkSignature")

        onProgress("Setting up output APK...")
        if (outputApk.exists()) {
            outputApk.delete()
        }

        ZFile.openReadWrite(outputApk, createZFileOptions()).use { dstFile ->
            ZFile.openReadOnly(sourceApk).use { srcFile ->
                onProgress("Registering signer...")
                registerSigner(dstFile)

                onProgress("Processing AndroidManifest.xml...")
                val manifestResult = processManifest(srcFile, dstFile)
                onProgress("packageName: ${manifestResult.packageName}")
                manifestResult.originalAppClass?.let {
                    onProgress("applicationName class: ${manifestResult.originalAppClass}")
                }
                onProgress("Copying source files...")
                copySourceEntries(srcFile, dstFile)

                onProgress("Adding path files...")
                addPatchFiles(dstFile, manifestResult, apkSignature)

                onProgress("Finalizing APK...")
            }
        }
    }

    private fun extractSignature(): String {
        return RandomAccessFile(sourceApk, "r").use { file ->
            SignerCompat.findAnySign(DataSources.asDataSource(file))
                ?: throw PatchException(PatchException.APK_NOT_SIGNED)
        }
    }

    private fun processManifest(srcFile: ZFile, dstFile: ZFile): ManifestResult {
        val srcManifest = srcFile.get(ApkPatcherConfig.MANIFEST_PATH)
            ?: throw PatchException(PatchException.APK_INVALID)

        val manifestEditor = srcManifest.open().use { stream ->
            ManifestEditor(stream.readBytes())
        }

        // Get original manifest and package name
        val manifestResult = manifestEditor.get()

        // Modify manifest to inject the path application class
        manifestEditor.setApplicationName(ApkPatcherConfig.DEFAULT_APP_NAME)

        // Write the modified manifest to the APK
        dstFile.add(ApkPatcherConfig.MANIFEST_PATH, manifestEditor.toByteArray().inputStream())

        return manifestResult
    }

    private fun copySourceEntries(srcFile: ZFile, dstFile: ZFile) {
        for (entry in srcFile.entries()) {
            val name = entry.centralDirectoryHeader.name

            if (shouldSkipEntry(name, dstFile)) {
                continue
            }

            val shouldCompress = determineCompression(name)

            dstFile.add(name, entry.open(), shouldCompress)
        }
    }

    private fun shouldSkipEntry(name: String, dstFile: ZFile): Boolean {
        return when {
            dstFile.get(name) != null -> true
            name == ApkPatcherConfig.MANIFEST_PATH -> true
            isSignatureFile(name) -> true
            else -> false
        }
    }

    private fun isSignatureFile(name: String): Boolean {
        return name.startsWith("META-INF/") &&
                (name.endsWith(".SF") || name.endsWith(".MF") || name.endsWith(".RSA"))
    }

    private fun determineCompression(name: String): Boolean {
        return when {
            // resources.arsc MUST be uncompressed for Android 11+
            name == ApkPatcherConfig.RESOURCES_ARSC -> false
            // Native libraries MUST be uncompressed for memory mapping
            name.endsWith(".so") -> false
            // Everything else can be compressed to save space
            else -> true
        }
    }

    private fun addPatchFiles(
        dstFile: ZFile,
        manifestResult: ManifestResult,
        apkSignature: String
    ) {
        // Add path configuration
        val config = createPatchConfig(manifestResult, apkSignature)
        dstFile.add(
            ApkPatcherConfig.CONFIG_PATH,
            Json.encodeToString(config).toByteArray().inputStream(),
            true
        )

        // Add original APK (compressed to save space)
        dstFile.add(
            ApkPatcherConfig.ORIGINAL_APK_PATH,
            sourceApk.inputStream(),
            true
        )

        // Add native library (uncompressed for proper alignment)
        val nativeLibrary = javaClass.getResourceAsStream("/libPatch")
            ?: throw PatchException(PatchException.SO_PATCH_LOAD_FAILED)
        dstFile.add(
            ApkPatcherConfig.LIB_PATH,
            nativeLibrary,
            false
        )

        // Add path DEX
        val patchDex = javaClass.getResourceAsStream("/dexPatch")
            ?: throw PatchException(PatchException.DEX_PATCH_LOAD_FAILED)

        dstFile.add(
            findNextClassesName(dstFile),
            patchDex,
            true
        )
    }

    private fun createPatchConfig(
        manifestResult: ManifestResult,
        apkSignature: String
    ): SRPatchConfig {
        return SRPatchConfig(
            apkSize = sourceApk.length(),
            originalApplicationName = manifestResult.originalAppClass,
            packageName = manifestResult.packageName,
            pathRedirectionEnabled = options.pathRedirectionEnabled,
            pmsProxyMethod = options.pmsProxyMethod.key,
            signature = apkSignature,
            signatureStrength = options.signatureStrength.level
        )
    }

    /**
     * Finds the next available classes*.dex filename
     */
    private fun findNextClassesName(zFile: ZFile): String {
        val regex = Regex("""classes(\d*)\.dex""")

        val maxIndex = zFile.entries()
            .mapNotNull { entry ->
                regex.matchEntire(entry.centralDirectoryHeader.name)
                    ?.groupValues
                    ?.get(1)
            }
            .mapNotNull { numberStr ->
                if (numberStr.isEmpty()) 1 else numberStr.toIntOrNull()
            }
            .maxOrNull() ?: 0

        val nextIndex = maxIndex + 1
        return if (nextIndex == 1) "classes.dex" else "classes$nextIndex.dex"
    }

    /**
     * Creates ZFile options with proper alignment rules
     */
    private fun createZFileOptions(): ZFileOptions {
        return ZFileOptions().apply {
            alignmentRule = AlignmentRules.compose(
                // Native libraries must be page-aligned (4096 bytes) for memory mapping
                AlignmentRules.constantForSuffix(".so", ApkPatcherConfig.PAGE_ALIGNMENT),
                // resources.arsc must be 4-byte aligned for Android 11+
                createResourcesArscAlignmentRule()
            )
            // Auto-sort files for optimal alignment
            autoSortFiles = true
        }
    }

    /**
     * Creates alignment rule for resources.arsc
     */
    private fun createResourcesArscAlignmentRule(): AlignmentRule {
        return AlignmentRule { path ->
            if (path == ApkPatcherConfig.RESOURCES_ARSC) {
                ApkPatcherConfig.RESOURCES_ALIGNMENT
            } else {
                AlignmentRule.NO_ALIGNMENT
            }
        }
    }

    /**
     * Registers APK signer with the ZFile
     */
    private fun registerSigner(zFile: ZFile) {
        try {
            val keyConfig = options.keystoreConfig
            val keyStore = KeyStore.getInstance("PKCS12")

            keyStore.load(
                keyConfig.storeSource.getStream(),
                keyConfig.keystorePassword.toCharArray()
            )

            val privateKey = keyStore.getKey(
                keyConfig.keyAlias,
                keyConfig.keyPassword.toCharArray()
            ) as PrivateKey

            val certificates = keyStore.getCertificateChain(keyConfig.keyAlias).mapNotNull {
                it as? X509Certificate
            }.toTypedArray()

            val signingOptions = SigningOptions.builder()
                .setMinSdkVersion(ApkPatcherConfig.MIN_SDK_VERSION)
                .setV1SigningEnabled(false)  // V1 not needed for Android 7.0+
                .setV2SigningEnabled(true)   // V2 signing for modern Android
                .setKey(privateKey)
                .setCertificates(*certificates)
                .build()

            SigningExtension(signingOptions).register(zFile)
        } catch (e: Exception) {
            throw PatchException(PatchException.APK_SIGNER_FAILED, e)
        }
    }
}