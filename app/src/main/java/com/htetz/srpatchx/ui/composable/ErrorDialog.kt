package com.htetz.srpatchx.ui.composable

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.htetz.core.PatchException
import com.htetz.srpatchx.R

@Composable
fun ErrorDialog(
    throwable: Throwable,
    title: String,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        icon = {
            Icon(Icons.TwoTone.Error, contentDescription = null)
        },
        title = { Text(title) },
        text = {
            Text(
                text = throwable.toLocalizedMessage(),
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.btn_close))
            }
        }
    )
}

/**
 * 如果异常是 core 模块的 [PatchException],会根据其 [PatchException.messageKey]
 * 从 strings.xml 中查找对应的本地化文案;否则回退为原始消息或完整堆栈。
 */
@Composable
private fun Throwable.toLocalizedMessage(): String {
    if (this is PatchException) {
        val resId = messageKeyToResourceId(messageKey)
        if (resId != null) {
            return stringResource(resId)
        }
    }
    return message.orEmpty().ifEmpty { stackTraceToString() }
}

private fun messageKeyToResourceId(key: String?): Int? = when (key) {
    PatchException.APK_NOT_SIGNED -> R.string.error_apk_not_signed
    PatchException.APK_INVALID -> R.string.error_apk_invalid
    PatchException.SO_PATCH_LOAD_FAILED -> R.string.error_so_patch_load_failed
    PatchException.DEX_PATCH_LOAD_FAILED -> R.string.error_dex_patch_load_failed
    PatchException.APK_SIGNER_FAILED -> R.string.error_apk_signer_failed
    PatchException.MANIFEST_PACKAGE_MISSING -> R.string.error_manifest_package_missing
    PatchException.MANIFEST_TAG_MISSING -> R.string.error_manifest_tag_missing
    PatchException.APPLICATION_TAG_MISSING -> R.string.error_application_tag_missing
    else -> null
}
