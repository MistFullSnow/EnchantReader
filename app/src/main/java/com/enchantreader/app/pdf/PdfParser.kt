package com.enchantreader.app.pdf

import android.content.Context
import android.net.Uri
import com.enchantreader.app.model.Act
import com.enchantreader.app.model.Scene
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream

object PdfParser {
    fun parseActsAndScenes(context: Context, uri: Uri): List<Act> {
        val text = extractText(context, uri)
        if (text.isBlank()) return emptyList()
        return splitIntoActsAndScenes(text)
    }

    private fun extractText(context: Context, uri: Uri): String {
        val resolver = context.contentResolver
        val input: InputStream = resolver.openInputStream(uri) ?: return ""
        input.use { stream ->
            PDDocument.load(stream).use { document ->
                val stripper = PDFTextStripper()
                stripper.sortByPosition = true
                return stripper.getText(document)
            }
        }
    }

    private fun splitIntoActsAndScenes(fullText: String): List<Act> {
        val lines = fullText.lines()
        val actRegex = Regex("^\\s*ACT\\s+([IVXLC]+)\\s*$", RegexOption.IGNORE_CASE)
        val sceneRegex = Regex("^\\s*SCENE\\s+([0-9IVXLC]+)\\s*[:.-]?\\s*(.*)$", RegexOption.IGNORE_CASE)

        val acts = mutableListOf<ActBuilder>()
        var currentAct = ActBuilder(title = "Prologue")
        var currentScene = SceneBuilder(title = "Scene 1")

        fun commitScene() {
            if (currentScene.text.isNotBlank()) {
                currentAct.scenes.add(Scene(title = currentScene.title, text = currentScene.text.trim()))
                currentScene = SceneBuilder(title = "Scene ${currentAct.scenes.size + 1}")
            }
        }
        fun commitAct() {
            commitScene()
            if (currentAct.scenes.isNotEmpty()) {
                acts.add(currentAct)
            }
            currentAct = ActBuilder(title = "Act ${acts.size + 1}")
        }

        for (line in lines) {
            when {
                actRegex.matches(line) -> {
                    commitAct()
                    val roman = actRegex.find(line)?.groupValues?.getOrNull(1)?.trim().orEmpty()
                    currentAct.title = "Act $roman"
                }
                sceneRegex.matches(line) -> {
                    commitScene()
                    val match = sceneRegex.find(line)
                    val index = match?.groupValues?.getOrNull(1)?.trim().orEmpty()
                    val subtitle = match?.groupValues?.getOrNull(2)?.trim().orEmpty()
                    currentScene.title = buildString {
                        append("Scene $index")
                        if (subtitle.isNotEmpty()) append(": ").append(subtitle)
                    }
                }
                else -> {
                    currentScene.text += line + "\n"
                }
            }
        }
        // finalize
        commitAct()

        // If nothing parsed, return single act with single scene
        if (acts.isEmpty()) {
            return listOf(
                Act(
                    title = "Full Text",
                    scenes = listOf(Scene(title = "Scene", text = fullText.trim()))
                )
            )
        }
        return acts.map { it.toAct() }
    }

    private data class ActBuilder(var title: String, val scenes: MutableList<Scene> = mutableListOf()) {
        fun toAct(): Act = Act(title = title, scenes = scenes.toList())
    }
    private data class SceneBuilder(var title: String, var text: String = "")
}
