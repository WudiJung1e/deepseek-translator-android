package com.example.translation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.translation.data.DeepSeekApi
import com.example.translation.data.TranslationHistoryEntry
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val targetLanguages = listOf(
    "中文",
    "English",
    "日本語",
    "한국어",
    "Français",
    "Deutsch",
)

@Composable
fun TranslatorScreen(
    api: DeepSeekApi,
    contentPadding: PaddingValues,
    scrollBehavior: ScrollBehavior,
    onTranslationSaved: (TranslationHistoryEntry) -> Unit,
) {
    var sourceText by remember { mutableStateOf("") }
    var targetLanguageIndex by remember {
        mutableIntStateOf(targetLanguages.indexOf("English").coerceAtLeast(0))
    }
    val targetLanguage = targetLanguages[targetLanguageIndex]
    var translatedText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var copyMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .pageScrollModifiers(scrollBehavior)
            .padding(horizontal = 12.dp),
        contentPadding = contentPadding,
    ) {
        item(key = "input_title") { SmallTitle(text = "输入") }
        item(key = "input_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                insideMargin = PaddingValues(16.dp),
            ) {
                TextField(
                    value = sourceText,
                    onValueChange = { sourceText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "要翻译的文本",
                    minLines = 5,
                )
            }
        }

        item(key = "language_title") { SmallTitle(text = "目标语言") }
        item(key = "language_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                insideMargin = PaddingValues(),
            ) {
                OverlayDropdownPreference(
                    title = "目标语言",
                    items = targetLanguages,
                    selectedIndex = targetLanguageIndex,
                    onSelectedIndexChange = { targetLanguageIndex = it },
                )
            }
        }

        item(key = "translate_button") {
            Button(
                onClick = {
                    if (sourceText.isBlank()) {
                        errorMessage = "请先输入要翻译的文本"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null
                    copyMessage = null
                    translatedText = ""

                    scope.launch {
                        runCatching {
                            api.translate(sourceText.trim(), targetLanguage)
                        }.onSuccess { result ->
                            translatedText = result
                            onTranslationSaved(
                                TranslationHistoryEntry(
                                    sourceText = sourceText.trim(),
                                    targetLanguage = targetLanguage,
                                    translatedText = result,
                                    createdAtMillis = currentTimeMillis(),
                                ),
                            )
                        }.onFailure { throwable ->
                            errorMessage = throwable.message ?: "翻译失败，请稍后重试"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColorsPrimary(),
            ) {
                Text(if (isLoading) "翻译中..." else "翻译")
            }
        }

        errorMessage?.let { message ->
            item(key = "error_message") {
                Text(
                    text = message,
                    color = Color(0xFFD32F2F),
                    style = MiuixTheme.textStyles.body2,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }
        }

        copyMessage?.let { message ->
            item(key = "copy_message") {
                Text(
                    text = message,
                    color = MiuixTheme.colorScheme.primary,
                    style = MiuixTheme.textStyles.body2,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }
        }

        if (translatedText.isNotBlank()) {
            item(key = "result_title") { SmallTitle(text = "结果") }
            item(key = "result_card") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    insideMargin = PaddingValues(),
                ) {
                    BasicComponent(
                        title = "翻译结果",
                        summary = "目标语言：$targetLanguage",
                        endActions = {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(translatedText))
                                    copyMessage = "已复制到剪贴板"
                                },
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Copy,
                                    contentDescription = "复制译文",
                                    tint = MiuixTheme.colorScheme.primary,
                                )
                            }
                        },
                    )
                    Text(
                        text = translatedText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
