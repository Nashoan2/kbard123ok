package com.openswift.keyboard.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.openswift.keyboard.data.KeyboardLanguages
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.data.SnippetManager
import com.openswift.keyboard.theme.Themes
import com.openswift.keyboard.theme.ThemeEditor

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings = Settings(this@SettingsActivity)
            val themeEditor = ThemeEditor(this@SettingsActivity)
            val snippets = SnippetManager(this@SettingsActivity)
            SettingsUI(settings, themeEditor, snippets)
        }
    }
}

@Composable
fun SettingsUI(
    settings: Settings,
    themeEditor: ThemeEditor,
    snippets: SnippetManager
) {
    val theme = themeEditor.resolve(settings.theme)
    val bgColor = ComposeColor(theme.background)
    val keyBgColor = ComposeColor(theme.keyBackground)
    val textColor = ComposeColor(theme.keyText)
    val accentColor = ComposeColor(theme.keyAccent)

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("لوحة المفاتيح", "السمات", "القصاصات")

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .safeDrawingPadding()
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SemanticColors.getSubtleAccent(accentColor, true),
                contentColor = accentColor,
                divider = { HorizontalDivider(color = accentColor.copy(alpha = Alphas.divider)) }
            ) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                label,
                                color = textColor,
                                style = if (selectedTab == idx) AppTypography.labelLarge else AppTypography.labelMedium
                            )
                        },
                        modifier = Modifier.padding(vertical = Spacing.md)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg)
            ) {
                when (selectedTab) {
                    0 -> KeyboardSettingsTab(settings, textColor, accentColor)
                    1 -> ThemesTab(settings, themeEditor, textColor, accentColor)
                    2 -> SnippetsTab(snippets, textColor, accentColor)
                }
            }
        }
    }
}

@Composable
fun KeyboardSettingsTab(settings: Settings, textColor: ComposeColor, accentColor: ComposeColor) {
    var language by remember { mutableStateOf(settings.language) }
    var layout by remember { mutableStateOf(settings.layout) }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(
            "إعدادات لوحة المفاتيح",
            style = AppTypography.headlineSmall,
            color = textColor
        )

        SettingsGroup(title = "اللغة") {
            KeyboardLanguages.all.forEach { option ->
                ToggleOption(option.name, language == option.code, textColor, accentColor) {
                    settings.language = option.code
                    language = settings.language
                    layout = settings.layout
                }
            }
        }

        SettingsGroup(title = "التخطيط") {
            listOf("arabic" to "العربية (Arabic)", "qwerty" to "الإنجليزية (QWERTY)").forEach { (id, label) ->
                ToggleOption(label, layout == id, textColor, accentColor) {
                    settings.layout = id
                    layout = id
                }
            }
        }

        SettingsGroup(title = "ارتفاع لوحة المفاتيح") {
            var currentHeight by remember { mutableStateOf(settings.keyHeightDp) }
            val heightOptions = listOf(
                58 to "صغير (58 dp)",
                68 to "متوسط (68 dp)",
                78 to "مرتفع - افتراضي (78 dp)",
                88 to "كبير (88 dp)",
                96 to "كبير جداً (96 dp)"
            )
            heightOptions.forEach { (dpValue, label) ->
                ToggleOption(label, currentHeight == dpValue, textColor, accentColor) {
                    settings.keyHeightDp = dpValue
                    currentHeight = dpValue
                }
            }
        }

        SettingsGroup(title = "الميزات") {
            var glide by remember { mutableStateOf(settings.glideEnabled) }
            var correct by remember { mutableStateOf(settings.autoCorrect) }
            var languageDetection by remember { mutableStateOf(settings.languageDetection) }
            var cap by remember { mutableStateOf(settings.autoCapitalize) }
            var haptic by remember { mutableStateOf(settings.hapticFeedback) }
            var sound by remember { mutableStateOf(settings.soundFeedback) }
            var reducedMotion by remember { mutableStateOf(settings.reducedMotion) }

            SwitchOption("الكتابة بالسحب السريع", glide, textColor, accentColor) { glide = it; settings.glideEnabled = it }
            SwitchOption("التصحيح التلقائي", correct, textColor, accentColor) { correct = it; settings.autoCorrect = it }
            SwitchOption("التعرف التلقائي على اللغة", languageDetection, textColor, accentColor) { languageDetection = it; settings.languageDetection = it }
            SwitchOption("بدء الجمل بحرف كبير", cap, textColor, accentColor) { cap = it; settings.autoCapitalize = it }
            SwitchOption("الاهتزاز عند اللمس", haptic, textColor, accentColor) { haptic = it; settings.hapticFeedback = it }
            SwitchOption("أصوات النقر", sound, textColor, accentColor) { sound = it; settings.soundFeedback = it }
            SwitchOption("تقليل الحركة والانتقالات", reducedMotion, textColor, accentColor) { reducedMotion = it; settings.reducedMotion = it }
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            title,
            style = AppTypography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.secondary)
        )
        content()
    }
}

@Composable
fun ThemesTab(settings: Settings, themeEditor: ThemeEditor, textColor: ComposeColor, accentColor: ComposeColor) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(
            "سمات الألوان",
            style = AppTypography.headlineSmall,
            color = textColor
        )

        SettingsGroup(title = "السمات المدمجة") {
            Themes.all.forEach { t ->
                ToggleOption(t.displayName, settings.theme == t.id, textColor, accentColor) { settings.theme = t.id }
            }
        }

        SettingsGroup(title = "السمات المخصصة") {
            val custom = remember { themeEditor.listCustom() }
            if (custom.isEmpty()) {
                Text(
                    "لا توجد سمات مخصصة بعد",
                    style = AppTypography.bodySmall,
                    color = textColor.copy(alpha = Alphas.secondary)
                )
            } else {
                custom.forEach { ct ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ct.name, color = textColor, style = AppTypography.bodyMedium)
                        IconButton(
                            onClick = { themeEditor.delete(ct.id) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = accentColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SnippetsTab(
    snippets: SnippetManager,
    textColor: ComposeColor,
    accentColor: ComposeColor,
    initialEditorOpen: Boolean = false
) {
    var allSnippets by remember { mutableStateOf(snippets.getAll()) }
    var editorOpen by remember { mutableStateOf(initialEditorOpen) }
    var editingSnippet by remember { mutableStateOf<SnippetManager.Snippet?>(null) }
    var trigger by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun openEditor(snippet: SnippetManager.Snippet? = null) {
        editingSnippet = snippet
        trigger = snippet?.trigger.orEmpty()
        replacement = snippet?.text.orEmpty()
        validationError = null
        editorOpen = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "القصاصات النصية",
                style = AppTypography.headlineSmall,
                color = textColor
            )
            Button(
                onClick = { openEditor() },
                shape = Shapes.md
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(Spacing.sm))
                Text("إنشاء", style = AppTypography.labelLarge)
            }
        }

        Text(
            "اكتب الكلمة المفتاحية متبوعة بمسافة أو إدخال لتوسيعها تلقائياً.",
            style = AppTypography.bodySmall,
            color = textColor.copy(alpha = Alphas.secondary),
            modifier = Modifier.padding(bottom = Spacing.md)
        )

        if (allSnippets.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                shape = Shapes.md,
                colors = CardDefaults.cardColors(
                    containerColor = SemanticColors.getSubtleAccent(accentColor, true)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "لا توجد قصاصات نصية بعد",
                        style = AppTypography.bodyMedium,
                        color = textColor,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )
                    Button(
                        onClick = { openEditor() },
                        shape = Shapes.md
                    ) {
                        Text("إنشاء قصاصة", style = AppTypography.labelLarge)
                    }
                }
            }
        } else {
            allSnippets.forEach { snippet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.md,
                    colors = CardDefaults.cardColors(
                        containerColor = SemanticColors.getSubtleAccent(accentColor, true)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevations.sm)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(snippet.trigger, style = AppTypography.labelLarge, color = accentColor)
                            Text(
                                snippet.text.replace("\n", " ").take(80),
                                style = AppTypography.bodySmall,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(
                            onClick = { openEditor(snippet) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "تعديل ${snippet.trigger}",
                                tint = accentColor
                            )
                        }
                        IconButton(
                            onClick = {
                                snippets.remove(snippet.trigger)
                                allSnippets = snippets.getAll()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "حذف ${snippet.trigger}",
                                tint = accentColor
                            )
                        }
                    }
                }
            }
        }
    }

    if (editorOpen) {
        AlertDialog(
            onDismissRequest = { editorOpen = false },
            title = {
                Text(if (editingSnippet == null) "إنشاء قصاصة" else "تعديل القصاصة")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedTextField(
                        value = trigger,
                        onValueChange = {
                            trigger = it
                            validationError = null
                        },
                        label = { Text("الكلمة المفتاحية") },
                        supportingText = {
                            Text("بدون مسافات، بحد أقصى ${SnippetManager.MAX_TRIGGER_LENGTH} حرفاً.")
                        },
                        isError = validationError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = replacement,
                        onValueChange = {
                            replacement = it
                            validationError = null
                        },
                        label = { Text("النص البديل الكامل") },
                        isError = validationError != null,
                        minLines = 3,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    validationError?.let { message ->
                        Text(
                            message,
                            color = MaterialTheme.colorScheme.error,
                            style = AppTypography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val error = snippets.save(
                            originalTrigger = editingSnippet?.trigger,
                            trigger = trigger,
                            text = replacement
                        )
                        if (error == null) {
                            allSnippets = snippets.getAll()
                            editorOpen = false
                        } else {
                            validationError = error
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { editorOpen = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SwitchOption(
    label: String,
    value: Boolean,
    textColor: ComposeColor,
    accentColor: ComposeColor,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = textColor, style = AppTypography.bodyMedium)
        Switch(
            checked = value,
            onCheckedChange = onValueChange,
            modifier = Modifier.scale(1.1f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = accentColor.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun ToggleOption(
    label: String,
    isSelected: Boolean,
    textColor: ComposeColor,
    accentColor: ComposeColor,
    onToggle: () -> Unit
) {
    Button(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = Shapes.md,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) accentColor else SemanticColors.getSubtleAccent(accentColor, true),
            contentColor = if (isSelected) ComposeColor.White else textColor
        )
    ) {
        Text(label, style = AppTypography.labelMedium)
    }
}
