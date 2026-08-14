package com.example.schedules.data.pdf

import android.net.Uri

interface IPdfExtractor {
    suspend fun extractText(uri: Uri): String
}
