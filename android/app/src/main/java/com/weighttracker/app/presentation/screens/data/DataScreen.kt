package com.weighttracker.app.presentation.screens.data

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.weighttracker.app.presentation.theme.AppColors

@Composable
fun DataScreen(
    onSettingsClick: () -> Unit = {},
    viewModel: DataViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImportClick(it) }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onRestoreClick(it) }
    }

    val migrationExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.onMigrationExport(it) }
    }

    // Import migration file launcher (JSON)
    val migrationImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onMigrationImport(it) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let { viewModel.onExport(it) }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (uiState.showClearConfirmDialog) {
        ClearDataConfirmDialog(
            onConfirm = viewModel::onConfirmClearData,
            onDismiss = viewModel::onDismissClearDialog
        )
    }

    if (uiState.showRestoreConfirmDialog) {
        RestoreConfirmDialog(
            onConfirm = viewModel::onConfirmRestore,
            onDismiss = viewModel::onDismissRestoreDialog
        )
    }

    // Migration Import confirmation dialog
    if (uiState.showMigrationImportConfirmDialog) {
        MigrationImportConfirmDialog(
            onConfirm = viewModel::onConfirmMigrationImport,
            onDismiss = viewModel::onDismissMigrationImportDialog
        )
    }

    if (uiState.showCleanupConfirmDialog) {
        CleanupConfirmDialog(
            onConfirm = viewModel::onConfirmCleanup,
            onDismiss = viewModel::onDismissCleanupDialog
        )
    }

    val context = LocalContext.current
    if (uiState.showShareDialog && uiState.exportedFilePath != null) {
        ExportSuccessDialog(
            filePath = uiState.exportedFilePath,
            onShare = {
                val shareIntent = viewModel.getShareIntent()
                shareIntent?.let {
                    ContextCompat.startActivity(context, Intent.createChooser(it, "分享文件"), null)
                }
                viewModel.clearExportedFilePath()
            },
            onDismiss = viewModel::clearExportedFilePath
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp)
        ) {
            TopBar(onSettingsClick = onSettingsClick)

            Spacer(Modifier.height(16.dp))

            StorageSection(
                storageSize = uiState.storageSize,
                lastBackupTime = uiState.lastBackupTime,
                onBackupClick = viewModel::onBackupClick,
                isBackingUp = uiState.isBackingUp,
                isRestoring = uiState.isRestoring,
                onRestoreClick = { restoreLauncher.launch(arrayOf("application/json", "*/*")) }
            )

            Spacer(Modifier.height(24.dp))

            ExcelSection(
                isExporting = uiState.isExporting,
                isImporting = uiState.isImporting,
                onExportClick = { exportLauncher.launch("体重记录_${System.currentTimeMillis()}.xlsx") },
                onImportClick = { 
                    importLauncher.launch(arrayOf(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-excel",
                        "*/*"
                    ))
                }
            )

            Spacer(Modifier.height(24.dp))

            DataToolsSection(
                onClearDataClick = viewModel::onClearDataClick,
                onCleanupClick = viewModel::onCleanupClick,
                isCleaningUp = uiState.isCleaningUp,
                onMigrationExportClick = { migrationExportLauncher.launch("体重记录_迁移_${System.currentTimeMillis()}.json") },
                onMigrationImportClick = { migrationImportLauncher.launch(arrayOf("application/json", "text/json", "*/*")) }
            )

            Spacer(Modifier.height(120.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TopBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Filled.Settings, contentDescription = "设置")
        }

        Text(
            text = "数据管理",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun IconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun StorageSection(
    storageSize: String,
    lastBackupTime: String?,
    onBackupClick: () -> Unit,
    isBackingUp: Boolean,
    isRestoring: Boolean,
    onRestoreClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "本地存储",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = storageSize,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "数据库占用空间",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "上次备份",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = lastBackupTime ?: "从未备份",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CloudUpload,
                    text = "立即备份",
                    isLoading = isBackingUp,
                    onClick = onBackupClick
                )
                ActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Restore,
                    text = "恢复数据",
                    isLoading = isRestoring,
                    onClick = onRestoreClick
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(enabled = !isLoading, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ExcelSection(
    isExporting: Boolean,
    isImporting: Boolean,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Column {
        Text(
            text = "Excel 操作",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExcelCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Upload,
                title = "导出为 Excel",
                description = "生成详细的数据报表",
                backgroundColor = MaterialTheme.colorScheme.primary,
                textColor = Color.White,
                isLoading = isExporting,
                onClick = onExportClick
            )

            ExcelCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.FileUpload,
                title = "从 Excel 导入",
                description = "支持批量导入历史记录",
                backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                textColor = MaterialTheme.colorScheme.onSurface,
                isLoading = isImporting,
                onClick = onImportClick
            )
        }
    }
}

@Composable
private fun ExcelCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    backgroundColor: Color,
    textColor: Color,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(120.dp)
            .clickable(enabled = !isLoading, onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (backgroundColor == MaterialTheme.colorScheme.primary)
                            Color.White.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (backgroundColor == MaterialTheme.colorScheme.primary)
                            Color.White else MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (backgroundColor == MaterialTheme.colorScheme.primary)
                            Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (backgroundColor == MaterialTheme.colorScheme.primary)
                        Color.White.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DataToolsSection(
    onClearDataClick: () -> Unit,
    onCleanupClick: () -> Unit,
    isCleaningUp: Boolean,
    onMigrationExportClick: () -> Unit,
    onMigrationImportClick: () -> Unit
) {
    Column {
        Text(
            text = "数据工具",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Column {
                DataToolItem(
                    icon = Icons.Filled.AutoDelete,
                    title = "自动清理旧数据",
                    description = "清理 1 年前的冗余记录",
                    isLoading = isCleaningUp,
                    onClick = onCleanupClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

                DataToolItem(
                    icon = Icons.Filled.MoveDown,
                    title = "导出迁移文件",
                    description = "将数据导出为迁移文件",
                    onClick = onMigrationExportClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

                DataToolItem(
                    icon = Icons.Filled.FileUpload,
                    title = "导入迁移文件",
                    description = "从迁移文件恢复数据",
                    onClick = onMigrationImportClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

                DataToolItem(
                    icon = Icons.Filled.DeleteForever,
                    title = "彻底清空数据",
                    description = "此操作不可撤销，请谨慎操作",
                    isDangerous = true,
                    onClick = onClearDataClick
                )
            }
        }
    }
}

@Composable
private fun DataToolItem(
    icon: ImageVector,
    title: String,
    description: String,
    isDangerous: Boolean = false,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDangerous) AppColors.Error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isDangerous) AppColors.Error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDangerous) AppColors.Error.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ClearDataConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认清空数据？") },
        text = {
            Text("清空后所有体重记录将被永久删除，此操作不可撤销。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认", color = AppColors.Error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun RestoreConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认恢复数据？") },
        text = {
            Text("恢复数据将覆盖当前所有记录，此操作不可撤销。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认恢复")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun MigrationImportConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认导入迁移文件？") },
        text = {
            Text("导入迁移文件将覆盖当前数据，并从迁移文件中导入记录。是否继续？")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun CleanupConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认清理旧数据？") },
        text = {
            Text("此操作将删除 1 年前的所有记录，此操作不可撤销。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认清理", color = AppColors.Error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ExportSuccessDialog(
    filePath: String?,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出成功") },
        text = {
            Column {
                Text("文件已保存")
                if (filePath != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = filePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onShare) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("分享")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
