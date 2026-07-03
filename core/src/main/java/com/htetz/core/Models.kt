package com.htetz.core

import kotlinx.serialization.Serializable
import java.io.InputStream

/**
 * PMS 代理方式。
 *
 * [key] 是稳定不变的字符串键,用于 JSON 配置序列化与反序列化。
 * 显示用标题/描述由 UI 层通过 `PMSProxyMethod.titleRes / descriptionRes` 扩展属性
 * 映射到 `R.string.*` (请参考 app 模块 `ResMapping.kt`)。
 */
enum class PMSProxyMethod(val key: String) {
    BINDER_PROXY(key = "IBINDER_PROXY"),
    CREATOR_PROXY(key = "CREATOR_PROXY"),
    ;

    companion object {
        fun fromKey(key: String?): PMSProxyMethod =
            values().firstOrNull { it.key == key } ?: BINDER_PROXY
    }
}

/**
 * 签名防护强度等级。
 *
 * [level] 用于 UI 展示与 JSON 配置序列化。显示用标题/描述请参考 app 模块 `ResMapping.kt`。
 */
enum class SignatureStrength(val level: Int, val key: String) {
    PMS_HOOK(level = 1, key = "PMS_HOOK"),
    IO_PMS_HOOK(level = 2, key = "IO_PMS_HOOK"),
    MORE_HOOK(level = 3, key = "MORE_HOOK"),
    SVC_HOOK(level = 4, key = "SVC_HOOK"),
    ;

    companion object {
        fun fromLevel(level: Int): SignatureStrength =
            values().firstOrNull { it.level == level } ?: SVC_HOOK
    }
}

data class ApkPatchOptions(
    val pathRedirectionEnabled: Boolean,
    val pmsProxyMethod: PMSProxyMethod,
    val signatureStrength: SignatureStrength,
    val keystoreConfig: KeystoreConfig,
) {
    companion object {
        val DEFAULT = ApkPatchOptions(
            pathRedirectionEnabled = false,
            pmsProxyMethod = PMSProxyMethod.BINDER_PROXY,
            signatureStrength = SignatureStrength.SVC_HOOK,
            keystoreConfig = KeystoreConfig.DEFAULT,
        )
    }
}

@Serializable
data class SRPatchConfig(
    val apkSize: Long,
    val originalApplicationName: String?,
    val packageName: String,
    val pathRedirectionEnabled: Boolean,
    val pmsProxyMethod: String,
    val signature: String,
    val signatureStrength: Int,
)

sealed class KeyStoreSource {
    data class FromFile(val path: String) : KeyStoreSource()
    data class FromResource(val name: String) : KeyStoreSource()

    fun getStream(): InputStream = when (this) {
        is FromFile -> java.io.File(path).inputStream()
        is FromResource -> javaClass.getResourceAsStream("/$name")
    }
}

data class KeystoreConfig(
    val storeSource: KeyStoreSource,
    val keystorePassword: String,
    val keyAlias: String,
    val keyPassword: String,
) {
    companion object {
        val DEFAULT = KeystoreConfig(
            storeSource = KeyStoreSource.FromResource("keystore.jks"),
            keystorePassword = "12345678",
            keyAlias = "patcher",
            keyPassword = "12345678",
        )
    }
}
