package com.htetz.core

import java.io.IOException

/**
 * 核心模块内部抛出的 Patch 相关异常。
 *
 * [messageKey] 是稳定的英文标识符,用于与 app 模块中的字符串资源 ID 对齐,
 * 以便 UI 根据当前系统语言显示本地化文案。如果 UI 层未匹配,则回退到 [messageKey] 原文。
 */
class PatchException(
    val messageKey: String,
    cause: Throwable? = null,
) : IOException(messageKey, cause) {
    companion object {
        // 与 app 模块 strings.xml 中的 error_* 资源一一对应
        const val APK_NOT_SIGNED = "Source APK is not signed"
        const val APK_INVALID = "Invalid APK: missing AndroidManifest.xml"
        const val SO_PATCH_LOAD_FAILED = "Failed to load .so patch"
        const val DEX_PATCH_LOAD_FAILED = "Failed to load dex patch"
        const val APK_SIGNER_FAILED = "Failed to register APK signer"
        const val MANIFEST_PACKAGE_MISSING = "Package attribute not found in manifest"
        const val MANIFEST_TAG_MISSING = "Manifest tag not found in AndroidManifest.xml"
        const val APPLICATION_TAG_MISSING = "Application tag not found in manifest"
    }
}
