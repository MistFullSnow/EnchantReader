package com.enchantreader.app.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun PdfPageReader(uri: Uri) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pageCount by remember { mutableStateOf(0) }
    val pages = remember { mutableStateListOf<Bitmap?>() }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            renderPagesIncrementally(context, uri) { count, bmp ->
                if (pageCount == 0) pageCount = count
                pages.add(bmp)
            }
        }
    }

    if (pageCount == 0 && pages.isEmpty()) {
        Text("Preparing pages…", style = MaterialTheme.typography.bodyMedium)
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(pages) { _, bmp ->
            bmp?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private fun renderPagesIncrementally(
    context: Context,
    uri: Uri,
    onPage: (pageCount: Int, bitmap: Bitmap) -> Unit
) {
    val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return
    pfd.use { fileDesc ->
        PdfRenderer(fileDesc).use { renderer ->
            val total = renderer.pageCount
            val scale = 2 // render at 2x
            for (i in 0 until total) {
                val page = renderer.openPage(i)
                val width = page.width * scale
                val height = page.height * scale
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                onPage(total, bitmap)
                // tiny delay to yield to UI
                Thread.sleep(5)
            }
        }
    }
}
