package com.htetz.srpatchx.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cable
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.htetz.core.ApkPatchOptions
import com.htetz.core.KeystoreConfig
import com.htetz.core.PMSProxyMethod
import com.htetz.core.SignatureStrength
import com.htetz.srpatchx.R
import com.htetz.srpatchx.domain.descriptionRes
import com.htetz.srpatchx.domain.titleRes
import com.htetz.srpatchx.models.FileItem
import com.htetz.srpatchx.ui.composable.ModifierExtensions.listCardContainerShape
import com.htetz.srpatchx.ui.composable.ModifierExtensions.listCardItemShape
import com.sebaslogen.resaca.rememberScoped
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkPatchSheet(
    apkItem: FileItem.Apk,
    onPatch: (ApkPatchOptions) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var patchOptions by rememberScoped {
        mutableStateOf(
            ApkPatchOptions(
                pathRedirectionEnabled = false,
                pmsProxyMethod = PMSProxyMethod.BINDER_PROXY,
                signatureStrength = SignatureStrength.SVC_HOOK,
                keystoreConfig = KeystoreConfig.DEFAULT,
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        PatchOptionsContent(
            apkItem = apkItem,
            patchOptions = patchOptions,
            onOptionsChanged = { patchOptions = it },
            onDismiss = {
                scope.launch { sheetState.hide() }
            },
            onPatch = { onPatch(patchOptions) },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PatchOptionsContent(
    apkItem: FileItem.Apk,
    patchOptions: ApkPatchOptions,
    onOptionsChanged: (ApkPatchOptions) -> Unit,
    onPatch: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        PatchHeader(apkItem)

        // Path Redirection Switch
        PathRedirectionSwitch(
            enabled = patchOptions.pathRedirectionEnabled,
            onEnabledChange = { onOptionsChanged(patchOptions.copy(pathRedirectionEnabled = it)) },
            modifier = Modifier
                .padding(vertical = 16.dp)
                .listCardContainerShape(),
        )

        // PMS Proxy Method Section
        Text(
            text = stringResource(R.string.section_pms_proxy_method),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        PMSProxyMethodOptions(
            pmsProxy = patchOptions.pmsProxyMethod,
            onMethodChange = { onOptionsChanged(patchOptions.copy(pmsProxyMethod = it)) },
        )

        // Signature Strength Section
        Text(
            text = stringResource(R.string.section_signature_strength),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        SignatureStrengthOptions(
            strength = patchOptions.signatureStrength,
            onMethodChange = { onOptionsChanged(patchOptions.copy(signatureStrength = it)) },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        PatchActions(
            onPatch = onPatch,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun PatchActions(
    onPatch: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.TwoTone.Cancel, null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.btn_cancel))
        }
        FilledTonalButton(
            onClick = onPatch,
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.TwoTone.Check, null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.btn_apply))
        }
    }
}

@Composable
private fun PatchHeader(
    apkItem: FileItem.Apk,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            model = apkItem,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium),
        )
        Column {
            Text(
                text = apkItem.appName,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = apkItem.packageName,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PathRedirectionSwitch(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier
            .selectable(
                selected = enabled,
                onClick = { onEnabledChange(!enabled) },
                role = Role.Switch,
            ),
        headlineContent = {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.padding(start = 16.dp),
                    ) {
                        Text(stringResource(R.string.badge_optional))
                    }
                },
            ) {
                Text(stringResource(R.string.title_path_redirection))
            }
        },
        supportingContent = { Text(stringResource(R.string.desc_path_redirection)) },
        leadingContent = {
            Icon(
                imageVector = Icons.TwoTone.Cable,
                contentDescription = null,
            )
        },
        trailingContent = {
            Switch(
                checked = enabled,
                onCheckedChange = null,
            )
        },
        colors = ListItemDefaults.colors(MaterialTheme.colorScheme.secondaryContainer),
    )
}

@Composable
private fun PMSProxyMethodOptions(
    pmsProxy: PMSProxyMethod,
    onMethodChange: (PMSProxyMethod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.listCardContainerShape(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PMSProxyMethod.entries.forEach { method ->
            RadioListItem(
                headlineContent = { Text(stringResource(method.titleRes)) },
                selected = method == pmsProxy,
                modifier = Modifier
                    .fillMaxWidth()
                    .listCardItemShape(),
                supportingContent = {
                    Text(stringResource(method.descriptionRes))
                },
                onClick = { onMethodChange(method) },
            )
        }
    }
}

@Composable
private fun SignatureStrengthOptions(
    strength: SignatureStrength,
    onMethodChange: (SignatureStrength) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.listCardContainerShape(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SignatureStrength.entries.forEach { item ->
            RadioListItem(
                headlineContent = {
                    Text(stringResource(item.titleRes))
                },
                selected = item == strength,
                modifier = Modifier
                    .fillMaxWidth()
                    .listCardItemShape(),
                supportingContent = {
                    Text(stringResource(item.descriptionRes))
                },
                trailingContent = {
                    Badge(
                        containerColor = when (item.level) {
                            1 -> MaterialTheme.colorScheme.primaryContainer
                            2 -> MaterialTheme.colorScheme.secondaryContainer
                            3 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        },
                    ) {
                        Text(stringResource(R.string.level_prefix, item.level))
                    }
                },
                onClick = { onMethodChange(item) },
            )
        }
    }
}

@Composable
private fun RadioListItem(
    headlineContent: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = headlineContent,
        leadingContent = {
            RadioButton(
                selected = selected,
                onClick = null,
            )
        },
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        overlineContent = overlineContent,
        modifier = modifier.selectable(
            selected = selected,
            onClick = onClick,
            role = Role.RadioButton,
        ),
        colors = ListItemDefaults.colors(
            containerColor = containerColor,
        ),
    )
}
