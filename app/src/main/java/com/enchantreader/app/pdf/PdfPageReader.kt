package com.enchantreader.app.pdf

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
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
import kotlinx.coroutines.withContext

@Composable
fun PdfPageReader(uri: Uri) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pageCount by remember { mutableStateOf(0) }
    var bitmaps by remember { mutableStateOf<List<Bitmap?>>(emptyList()) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            val (count, pages) = renderAllPages(context, uri)
            pageCount = count
            bitmaps = pages
        }
    }

    if (pageCount == 0) {
        Text("Couldn't extract text; rendering pages…", style = MaterialTheme.typography.bodyMedium)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(bitmaps) { _, bmp ->
            bmp?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private fun openParcelFileDescriptor(context: Context, uri: Uri): ParcelFileDescriptor? {
    return context.contentResolver.openFileDescriptor(uri, "r")
}

private fun renderAllPages(context: Context, uri: Uri): Pair<Int, List<Bitmap?>> {
    val pfd = openParcelFileDescriptor(context, uri) ?: return 0 to emptyList()
    pfd.use { fileDesc ->
        PdfRenderer(fileDesc).use { renderer ->
            val results = mutableListOf<Bitmap?>()
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val width = (page.width * 2)
                val height = (page.height * 2)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                results.add(bitmap)
            }
            return renderer.pageCount to results
        }
    }
}
