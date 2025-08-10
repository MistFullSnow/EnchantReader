package com.enchantreader.app.model

data class Scene(
    val title: String,
    val text: String
)

data class Act(
    val title: String,
    val scenes: List<Scene>
)
