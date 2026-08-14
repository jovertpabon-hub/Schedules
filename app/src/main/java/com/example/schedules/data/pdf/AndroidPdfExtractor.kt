package com.example.schedules.data.pdf

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPdfExtractor(private val context: Context) : IPdfExtractor {

    override suspend fun extractText(uri: Uri): String = withContext(Dispatchers.IO) {
        PDFBoxResourceLoader.init(context)
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream).use { pdDocument ->
                    val stripper = PDFTextStripper()
                    stripper.sortByPosition = true
                    val text = stripper.getText(pdDocument)
                    Log.d("AndroidPdfExtractor", "Extracted Text Length: ${text.length}")
                    text
                }
            } ?: ""
        } catch (e: Exception) {
            Log.e("AndroidPdfExtractor", "Error reading PDF", e)
            ""
        }
    }
}
