package com.enchantreader.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.enchantreader.app.model.Act
import com.enchantreader.app.model.Scene
import com.enchantreader.app.pdf.PdfParser
import com.enchantreader.app.pdf.PdfPageReader
import com.enchantreader.app.ui.components.GlassCard
import com.enchantreader.app.ui.theme.EnchantReaderTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Immersive full-screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent { EnchantReaderRoot() }
    }
}

@Composable
private fun EnchantReaderRoot() {
    EnchantReaderTheme {
        val actsState = remember { mutableStateOf<List<Act>>(emptyList()) }
        val loading = remember { mutableStateOf(false) }
        val error = remember { mutableStateOf<String?>(null) }
        val selectedScene = remember { mutableStateOf<Scene?>(null) }
        val lastUri = remember { mutableStateOf<Uri?>(null) }
        val usePageFallback = remember { mutableStateOf(false) }

        val context = LocalContext.current
        val pickPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                loading.value = true
                error.value = null
                selectedScene.value = null
                usePageFallback.value = false
                lastUri.value = uri
                try {
                    val acts = PdfParser.parseActsAndScenes(context, uri)
                    actsState.value = acts
                    val firstScene = acts.firstOrNull()?.scenes?.firstOrNull()
                    if (firstScene != null && firstScene.text.trim().length >= 100) {
                        selectedScene.value = firstScene
                    } else {
                        // Fallback to rendering pages if text is empty/too short
                        usePageFallback.value = true
                    }
                } catch (t: Throwable) {
                    // Parsing failed; try page rendering
                    usePageFallback.value = true
                    error.value = null
                } finally {
                    loading.value = false
                }
            }
        }

        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedBackground()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    TopBar(onSelectPdf = { pickPdf.launch(arrayOf("application/pdf")) })
                    Spacer(Modifier.height(12.dp))

                    when {
                        loading.value -> CenteredText(text = "Parsing…")
                        usePageFallback.value && lastUri.value != null -> PdfPageReader(uri = lastUri.value!!)
                        selectedScene.value != null -> ReaderPane(
                            acts = actsState.value,
                            selected = selectedScene.value!!,
                            onSelect = { selectedScene.value = it }
                        )
                        else -> WelcomePane(onSelectPdf = { pickPdf.launch(arrayOf("application/pdf")) })
                    }

                    error.value?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(onSelectPdf: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "EnchantReader",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
        )
        Spacer(Modifier.weight(1f))
        Button(onClick = onSelectPdf) { Text("Select PDF") }
    }
}

@Composable
private fun WelcomePane(onSelectPdf: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "A magical way to read your Cursed Child PDF",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap Select PDF to begin. We'll detect Acts and Scenes and, if needed, render pages directly.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onSelectPdf) { Text("Select PDF") }
        }
    }
}

@Composable
private fun ReaderPane(acts: List<Act>, selected: Scene, onSelect: (Scene) -> Unit) {
    Row(modifier = Modifier.fillMaxSize()) {
        GlassCard(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight()
                .padding(end = 8.dp)
        ) {
            LazyColumn {
                acts.forEach { act ->
                    item {
                        Text(
                            text = act.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    items(act.scenes) { scene ->
                        SceneRow(scene = scene, selected = scene.title == selected.title) {
                            onSelect(scene)
                        }
                    }
                    item { Spacer(Modifier.height(10.dp)) }
                }
            }
        }

        GlassCard(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight()
                .padding(start = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = selected.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = selected.text,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

@Composable
private fun SceneRow(scene: Scene, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = scene.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun AnimatedBackground() {
    val colors = listOf(
        Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)
    )
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(colors)))

    val particleCount = 40
    val particles = remember { List(particleCount) { Particle.random() } }
    val transition = rememberInfiniteTransition(label = "bg")
    val anim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        particles.forEach { p ->
            val xRaw = p.x * width + anim.value * p.vx * width
            val yRaw = p.y * height + anim.value * p.vy * height
            val x = ((xRaw % width) + width) % width
            val y = ((yRaw % height) + height) % height
            drawCircle(color = p.color, radius = p.radius, center = Offset(x, y))
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val color: Color
) {
    companion object {
        fun random(): Particle {
            return Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = Random.nextFloat() * 0.1f - 0.05f,
                vy = Random.nextFloat() * 0.1f - 0.05f,
                radius = Random.nextFloat() * 6f + 2f,
                color = listOf(
                    Color(0xFFB69DF8),
                    Color(0xFF89D1FF),
                    Color(0xFF80EEC0)
                ).random()
            )
        }
    }
}

@Composable
private fun CenteredText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
