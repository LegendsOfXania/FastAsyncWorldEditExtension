package fr.xania.utils

import java.io.File

fun ensureSchematicsFolderExists(): Boolean {
    val folder = File("plugins/Typewriter/assets/schematics")
    if (!folder.exists()) {
        val created = folder.mkdirs()
    }
    return true
}