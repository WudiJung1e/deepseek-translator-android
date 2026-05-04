package com.example.translation.data

data class TranslationHistoryEntry(
    val sourceText: String,
    val targetLanguage: String,
    val translatedText: String,
    val createdAtMillis: Long,
)
