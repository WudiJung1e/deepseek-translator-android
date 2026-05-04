package com.example.translation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.translation.data.TranslationHistoryEntry
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(1000)
                showSplash = false
            }

            val preferences = remember {
                getSharedPreferences("translation_settings", MODE_PRIVATE)
            }
            var apiKey by remember {
                mutableStateOf(
                    preferences.getString("deepseek_api_key", null).orEmpty(),
                )
            }
            var colorMode by remember {
                mutableStateOf(
                    when {
                        preferences.contains("color_mode") -> preferences.getInt("color_mode", 0)
                        preferences.contains("follow_system_colors") ->
                            if (preferences.getBoolean("follow_system_colors", true)) 0 else 1
                        else -> 0
                    },
                )
            }
            var historyEntries by remember {
                mutableStateOf(loadHistory(preferences.getString("translation_history", "[]").orEmpty()))
            }

            Box(modifier = Modifier.fillMaxSize()) {
                App(
                    apiKey = apiKey,
                    colorMode = colorMode,
                    historyEntries = historyEntries,
                    onApiKeyChanged = { newApiKey ->
                        val trimmedApiKey = newApiKey.trim()
                        preferences.edit()
                            .putString("deepseek_api_key", trimmedApiKey)
                            .apply()
                        apiKey = trimmedApiKey
                    },
                    onColorModeChanged = { mode ->
                        preferences.edit()
                            .putInt("color_mode", mode)
                            .apply()
                        colorMode = mode
                    },
                    onHistoryEntryAdded = { entry ->
                        historyEntries = (listOf(entry) + historyEntries).take(50)
                        preferences.edit()
                            .putString("translation_history", saveHistory(historyEntries))
                            .apply()
                    },
                    onHistoryCleared = {
                        historyEntries = emptyList()
                        preferences.edit()
                            .putString("translation_history", "[]")
                            .apply()
                    },
                )

                if (showSplash) {
                    LaunchMask()
                }
            }
        }
    }

    private fun loadHistory(rawHistory: String): List<TranslationHistoryEntry> {
        return runCatching {
            val array = JSONArray(rawHistory)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                TranslationHistoryEntry(
                    sourceText = item.optString("sourceText"),
                    targetLanguage = item.optString("targetLanguage"),
                    translatedText = item.optString("translatedText"),
                    createdAtMillis = item.optLong("createdAtMillis"),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun saveHistory(historyEntries: List<TranslationHistoryEntry>): String {
        val array = JSONArray()
        historyEntries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("sourceText", entry.sourceText)
                    .put("targetLanguage", entry.targetLanguage)
                    .put("translatedText", entry.translatedText)
                    .put("createdAtMillis", entry.createdAtMillis),
            )
        }
        return array.toString()
    }
}

@Composable
private fun LaunchMask() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.Black),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(240.dp),
            contentScale = ContentScale.Fit,
        )
    }
}
