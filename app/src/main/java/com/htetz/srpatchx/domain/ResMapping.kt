package com.htetz.srpatchx.domain

import androidx.annotation.StringRes
import com.htetz.core.PMSProxyMethod
import com.htetz.core.PMSProxyMethod.BINDER_PROXY
import com.htetz.core.PMSProxyMethod.CREATOR_PROXY
import com.htetz.core.SignatureStrength
import com.htetz.core.SignatureStrength.IO_PMS_HOOK
import com.htetz.core.SignatureStrength.MORE_HOOK
import com.htetz.core.SignatureStrength.PMS_HOOK
import com.htetz.core.SignatureStrength.SVC_HOOK
import com.htetz.srpatchx.R

/**
 * 将 core 模块的枚举映射到 app 模块的字符串资源 ID,以支持国际化。
 */
val PMSProxyMethod.titleRes: Int
    @StringRes
    get() = when (this) {
        BINDER_PROXY -> R.string.pms_binder_proxy_title
        CREATOR_PROXY -> R.string.pms_creator_proxy_title
    }

val PMSProxyMethod.descriptionRes: Int
    @StringRes
    get() = when (this) {
        BINDER_PROXY -> R.string.pms_binder_proxy_desc
        CREATOR_PROXY -> R.string.pms_creator_proxy_desc
    }

val SignatureStrength.titleRes: Int
    @StringRes
    get() = when (this) {
        PMS_HOOK -> R.string.sig_pms_hook_title
        IO_PMS_HOOK -> R.string.sig_io_pms_hook_title
        MORE_HOOK -> R.string.sig_more_hook_title
        SVC_HOOK -> R.string.sig_svc_hook_title
    }

val SignatureStrength.descriptionRes: Int
    @StringRes
    get() = when (this) {
        PMS_HOOK -> R.string.sig_pms_hook_desc
        IO_PMS_HOOK -> R.string.sig_io_pms_hook_desc
        MORE_HOOK -> R.string.sig_more_hook_desc
        SVC_HOOK -> R.string.sig_svc_hook_desc
    }
