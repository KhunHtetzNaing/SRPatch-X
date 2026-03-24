package com.htetz.srpatchx.ui.composable

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import com.htetz.srpatchx.R
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
) {
    val resource = LocalResources.current
    val readme by produceState(initialValue = "") {
        value = resource.openRawResource(R.raw.readme).bufferedReader().use { it.readText() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        ListItem(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            headlineContent = {
                MarkdownText(
                    markdown = readme,
                    linkColor = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}