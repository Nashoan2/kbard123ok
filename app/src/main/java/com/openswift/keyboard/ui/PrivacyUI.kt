package com.openswift.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openswift.keyboard.data.ClipboardHistory
import com.openswift.keyboard.data.TypedDataStores
import com.openswift.keyboard.engine.UserDictionary

@Composable
fun PrivacyUI(
    clipboardHistory: ClipboardHistory,
    userDict: UserDictionary,
    bgColor: Color,
    textColor: Color,
    accentColor: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var clipboardItems by remember { mutableStateOf(clipboardHistory.items()) }
    var wordCount by remember { mutableStateOf(userDict.getWordCount()) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
    ) {
        // Header
        Text(
            "الخصوصية والبيانات",
            style = AppTypography.displayMedium,
            color = textColor,
            modifier = Modifier.padding(bottom = Spacing.md)
        )
        
        Text(
            "اطّلع على ما يحفظه OpenSwift، أو قم بإعادة ضبطه أو حذفه. كل شيء يبقى على جهازك فقط.",
            style = AppTypography.bodyMedium,
            color = textColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = Spacing.lg)
        )

        // Clipboard History Section
        PrivacyDataCard(
            icon = Icons.AutoMirrored.Filled.List,
            title = "سجل الحافظة",
            subtitle = "${clipboardItems.size} نصوص محفوظة",
            bgColor = bgColor,
            accentColor = accentColor,
            textColor = textColor
        ) {
            if (clipboardItems.isEmpty()) {
                Text(
                    "لا توجد نصوص في الحافظة بعد. عند تفعيل سجل الحافظة، ستظهر النصوص المنسوخة هنا.",
                    style = AppTypography.bodySmall,
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(Spacing.md)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    clipboardItems.take(5).forEach { item ->
                        ClipboardItemRow(item, textColor)
                    }
                    if (clipboardItems.size > 5) {
                        Text(
                            "...و ${clipboardItems.size - 5} عناصر أخرى",
                            style = AppTypography.labelSmall,
                            color = textColor.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = Spacing.md)
                        )
                    }
                }
            }
            
            Button(
                onClick = { showClearConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor.copy(alpha = 0.15f),
                    contentColor = accentColor
                ),
                shape = Shapes.sm
            ) {
                Text("مسح الحافظة", style = AppTypography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Dictionary Stats Section
        PrivacyDataCard(
            icon = Icons.Filled.Edit,
            title = "التعلم والقاموس المحلي",
            subtitle = "$wordCount كلمات متعلمة",
            bgColor = bgColor,
            accentColor = accentColor,
            textColor = textColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                StatsRow("الكلمات المتعلمة", wordCount.toString(), textColor)
                HorizontalDivider(
                    color = textColor.copy(alpha = Alphas.divider),
                    modifier = Modifier.padding(vertical = Spacing.sm)
                )
                Text(
                    "يتعلم جهازك من أسلوب كتابتك لتقديم اقتراحات وتنبؤات أدق محلياً وبدون إنترنت.",
                    style = AppTypography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            
            Button(
                onClick = {
                    userDict.reset()
                    wordCount = 0
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor.copy(alpha = 0.15f),
                    contentColor = accentColor
                ),
                shape = Shapes.sm
            ) {
                Text("إعادة ضبط القاموس", style = AppTypography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Data Deletion Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.md,
            colors = CardDefaults.cardColors(
                containerColor = SemanticColors.getSubtleAccent(Color(0xFFF44336), true)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevations.sm)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                    )
                    Text(
                        "حذف جميع البيانات",
                        style = AppTypography.headlineSmall,
                        color = Color(0xFFC62828),
                    )
                }
                
                Text(
                    "حذف دائم لسجل الحافظة، الكلمات المتعلمة، القصاصات النصية، السمات والتخطيطات المخصصة، وسجل التعبيرات. لا يمكن التراجع عن هذا الإجراء.",
                    style = AppTypography.bodySmall,
                    color = textColor.copy(alpha = 0.8f)
                )
                
                Button(
                    onClick = { showDeleteAllConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    shape = Shapes.sm
                ) {
                    Text("حذف كافة البيانات نهائياً", style = AppTypography.labelMedium, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        // Privacy Policy Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor.copy(alpha = 0.05f), Shapes.sm)
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                "لا يملك تطبيق OpenSwift أي إذن للاتصال بالإنترنت. بيانات كتابتك لا تغادر جهازك أبداً.",
                style = AppTypography.labelSmall,
                color = textColor.copy(alpha = 0.68f),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
    }

    // Confirmation dialogs
    if (showClearConfirmation) {
        ConfirmationDialog(
            title = "مسح سجل الحافظة؟",
            message = "سيؤدي هذا إلى حذف جميع العناصر البالغ عددها ${clipboardItems.size}. لا يمكن التراجع عن هذا الإجراء.",
            onConfirm = {
                clipboardHistory.clear()
                clipboardItems = emptyList()
                showClearConfirmation = false
            },
            onDismiss = { showClearConfirmation = false },
            accentColor = accentColor,
            textColor = textColor,
            bgColor = bgColor
        )
    }

    if (showDeleteAllConfirmation) {
        ConfirmationDialog(
            title = "حذف كافة البيانات؟",
            message = "سيؤدي هذا إلى حذف دائم لجميع بيانات الكتابة والتخصيص المحفوظة على هذا الجهاز.",
            onConfirm = {
                clipboardHistory.clear()
                userDict.reset()
                TypedDataStores.clearAll(context)
                clipboardItems = emptyList()
                wordCount = 0
                showDeleteAllConfirmation = false
            },
            onDismiss = { showDeleteAllConfirmation = false },
            accentColor = Color(0xFFF44336),
            textColor = textColor,
            bgColor = bgColor,
            isDangerous = true
        )
    }
}

@Composable
fun PrivacyDataCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    bgColor: Color,
    accentColor: Color,
    textColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.md,
        colors = CardDefaults.cardColors(
            containerColor = SemanticColors.getSubtleAccent(accentColor, true)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.sm)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = AppTypography.headlineSmall, color = textColor)
                    Text(subtitle, style = AppTypography.labelMedium, color = textColor.copy(alpha = 0.7f))
                }
            }
            
            HorizontalDivider(
                color = textColor.copy(alpha = Alphas.divider),
                modifier = Modifier.fillMaxWidth()
            )
            
            content()
        }
    }
}

@Composable
fun ClipboardItemRow(item: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "•",
            style = AppTypography.bodyMedium,
            color = textColor.copy(alpha = 0.5f),
            modifier = Modifier.padding(end = Spacing.sm)
        )
        Text(
            item.take(50) + if (item.length > 50) "…" else "",
            style = AppTypography.bodySmall,
            color = textColor,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsRow(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = AppTypography.bodyMedium, color = textColor)
        Text(value, style = AppTypography.labelLarge, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    accentColor: Color,
    textColor: Color,
    bgColor: Color,
    isDangerous: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, style = AppTypography.headlineSmall, color = textColor)
        },
        text = {
            Text(message, style = AppTypography.bodyMedium, color = textColor.copy(alpha = 0.8f))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDangerous) Color(0xFFF44336) else accentColor
                ),
                shape = Shapes.sm
            ) {
                Text("تأكيد", style = AppTypography.labelMedium, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = Shapes.sm
            ) {
                Text("إلغاء", style = AppTypography.labelMedium)
            }
        },
        containerColor = bgColor,
        shape = Shapes.md
    )
}
