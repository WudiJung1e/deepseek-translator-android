package com.example.translation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val colorModeOptions = listOf("跟随系统", "浅色", "深色")

@Composable
fun SettingsScreen(
    apiKey: String,
    colorMode: Int,
    onApiKeyChanged: (String) -> Unit,
    onColorModeChanged: (Int) -> Unit,
    contentPadding: PaddingValues,
    scrollBehavior: ScrollBehavior,
) {
    var apiKeyDraft by remember(apiKey) { mutableStateOf(apiKey) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val apiKeyConfigured = apiKey.isNotBlank()
    var isEditingApiKey by remember(apiKeyConfigured) {
        mutableStateOf(!apiKeyConfigured)
    }

    LazyColumn(
        modifier = Modifier
            .pageScrollModifiers(scrollBehavior)
            .padding(horizontal = 12.dp),
        contentPadding = contentPadding,
    ) {
        item(key = "deepseek_title") { SmallTitle(text = "DeepSeek") }
        item(key = "deepseek_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                insideMargin = PaddingValues(),
            ) {
                BasicComponent(
                    title = "API Key",
                    summary = if (apiKeyConfigured) "已配置" else "未配置",
                    endActions = {
                        IconButton(
                            onClick = {
                                apiKeyDraft = apiKey
                                saveMessage = null
                                isEditingApiKey = !isEditingApiKey
                            },
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Edit,
                                contentDescription = "修改 API Key",
                                tint = MiuixTheme.colorScheme.primary,
                            )
                        }
                    },
                )

                if (isEditingApiKey) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TextField(
                            value = apiKeyDraft,
                            onValueChange = {
                                apiKeyDraft = it
                                saveMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = "DeepSeek API Key",
                            singleLine = true,
                        )
                        Button(
                            onClick = {
                                onApiKeyChanged(apiKeyDraft)
                                saveMessage = "已保存"
                                isEditingApiKey = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColorsPrimary(),
                        ) {
                            Text("保存 API Key")
                        }
                        saveMessage?.let { message ->
                            Text(
                                text = message,
                                color = MiuixTheme.colorScheme.primary,
                                style = MiuixTheme.textStyles.body2,
                            )
                        }
                    }
                }

                BasicComponent(
                    title = "模型",
                    summary = "deepseek-v4-flash",
                )
            }
        }

        item(key = "appearance_title") { SmallTitle(text = "外观") }
        item(key = "appearance_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                insideMargin = PaddingValues(),
            ) {
                OverlayDropdownPreference(
                    title = "颜色模式",
                    items = colorModeOptions,
                    selectedIndex = colorMode.coerceIn(0, colorModeOptions.lastIndex),
                    onSelectedIndexChange = onColorModeChanged,
                )
            }
        }

        item(key = "about_title") { SmallTitle(text = "说明") }
        item(key = "about_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                insideMargin = PaddingValues(),
            ) {
                BasicComponent(
                    title = "API Key 保存位置",
                    summary = "API Key 只会保存到手机本地设置中，打包 APK 不会内置你的 Key。",
                )
            }
        }
    }
}
