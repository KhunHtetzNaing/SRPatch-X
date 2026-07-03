package com.htetz.srpatchx.ui.composable

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.InsertDriveFile
import androidx.compose.material.icons.twotone.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.htetz.srpatchx.domain.toReadableSize
import com.htetz.srpatchx.domain.toReadableTime
import com.htetz.srpatchx.models.FileItem
import java.io.File
@Composable
fun FileItemCard(
    item: FileItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (item.file.isDirectory)
        FolderItem(item.file, onClick, onLongClick, modifier)
    else
        FileItem(item, onClick, onLongClick, modifier)
}

@Composable
private fun FolderItem(
    file: File,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
            leadingContent = {
                Icon(
                    imageVector = Icons.TwoTone.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            headlineContent = {
                Text(text = file.name, overflow = TextOverflow.MiddleEllipsis, maxLines = 1)
            },
            supportingContent = {
                val ctx = LocalContext.current
                Text(file.lastModified().toReadableTime(ctx))
            }
        )
    }
}

@Composable
private fun FileItem(
    item: FileItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val file = item.file
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
            leadingContent = {
                when (item) {
                    is FileItem.Apk -> AsyncImage(
                        model = item,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )

                    is FileItem.Regular -> Icon(
                        imageVector = Icons.AutoMirrored.TwoTone.InsertDriveFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            headlineContent = {
                Text(text = file.name, overflow = TextOverflow.MiddleEllipsis, maxLines = 1)
            },
            supportingContent = {
                val ctx = LocalContext.current
                Text(
                    "${file.length().toReadableSize(ctx)}, ${file.lastModified().toReadableTime(ctx)}"
                )
            }
        )
    }
}