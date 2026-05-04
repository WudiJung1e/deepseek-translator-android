package com.example.translation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.translation.data.TranslationHistoryEntry
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HistoryScreen(
    historyEntries: List<TranslationHistoryEntry>,
    onHistoryCleared: () -> Unit,
    contentPadding: PaddingValues,
    scrollBehavior: ScrollBehavior,
) {
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .pageScrollModifiers(scrollBehavior)
            .padding(horizontal = 12.dp),
        contentPadding = contentPadding,
    ) {
        if (historyEntries.isEmpty()) {
            item(key = "empty") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    insideMargin = PaddingValues(),
                ) {
                    BasicComponent(
                        title = "暂无历史记录",
                        summary = "翻译成功后会自动保存在这里。",
                    )
                }
            }
        } else {
            item(key = "summary_title") { SmallTitle(text = "概览") }
            item(key = "summary_card") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    insideMargin = PaddingValues(),
                ) {
                    BasicComponent(
                        title = "已保存翻译",
                        summary = "${historyEntries.size} 条",
                        endActions = {
                            TextButton(
                                text = "清空",
                                onClick = onHistoryCleared,
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                            )
                        },
                    )
                }
            }

            item(key = "list_title") { SmallTitle(text = "历史记录") }

            items(
                count = historyEntries.size,
                key = { index -> historyEntries[index].createdAtMillis },
            ) { index ->
                val entry = historyEntries[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    insideMargin = PaddingValues(),
                ) {
                    BasicComponent(
                        title = "翻译为 ${entry.targetLanguage}",
                        summary = entry.sourceText,
                        endActions = {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(entry.translatedText))
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
                        text = entry.translatedText,
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
