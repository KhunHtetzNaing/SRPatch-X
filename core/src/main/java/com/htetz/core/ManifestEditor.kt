package com.htetz.core

import pxb.android.axml.Axml
import pxb.android.axml.AxmlReader
import pxb.android.axml.AxmlWriter

internal data class ManifestResult(
    val packageName: String,
    val originalAppClass: String?,
)

internal class ManifestEditor(manifestBytes: ByteArray) {

    private val aXML: Axml = parseManifest(manifestBytes)

    private val manifest: Axml.Node by lazy {
        findManifestNode()
    }

    private val application: Axml.Node by lazy {
        findApplicationNode()
    }

    fun getPackageName(): String {
        return manifest.attrs
            .find { it.name == "package" }
            ?.value as? String
            ?: throw PatchException(PatchException.MANIFEST_PACKAGE_MISSING)
    }

    fun getApplicationName(): String? {
        return getApplicationNameAttr()?.value as? String
    }

    fun get(): ManifestResult {
        return ManifestResult(getPackageName(), getApplicationName())
    }

    fun setApplicationName(appClassName: String) {
        val nameAttr = getApplicationNameAttr()

        if (nameAttr != null) {
            // Update existing attribute
            nameAttr.value = appClassName
        } else {
            // Add new attribute
            application.attr(
                ANDROID_NAMESPACE,
                "name",
                ATTR_NAME_RES_ID,
                TYPE_STRING,
                appClassName
            )
        }
    }

    fun toByteArray(): ByteArray {
        return AxmlWriter().apply {
            aXML.accept(this)
        }.toByteArray()
    }

    // Private helper methods
    private fun parseManifest(manifestBytes: ByteArray): Axml {
        return Axml().apply {
            AxmlReader(manifestBytes).accept(this)
        }
    }

    private fun findManifestNode(): Axml.Node {
        return aXML.firsts.find { it.name == "manifest" }
            ?: throw PatchException(PatchException.MANIFEST_TAG_MISSING)
    }

    private fun findApplicationNode(): Axml.Node {
        return manifest.children.find { it.name == "application" }
            ?: throw PatchException(PatchException.APPLICATION_TAG_MISSING)
    }

    private fun getApplicationNameAttr(): Axml.Node.Attr? {
        return application.attrs.find { it.name == "name" }
    }

    companion object {
        // Android manifest XML namespace
        private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

        // Resource ID for the android:name attribute
        private const val ATTR_NAME_RES_ID = 0x01010003

        // Type identifier for string values
        private const val TYPE_STRING = 0x03
    }
}
