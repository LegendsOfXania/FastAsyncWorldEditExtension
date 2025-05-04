package fr.xania.utils

import com.typewritermc.core.extension.Initializable
import com.typewritermc.core.extension.annotations.Singleton

@Singleton
object SchematicsInitilizer : Initializable {
    override suspend fun initialize() {
        ensureSchematicsFolderExists()
    }

    override suspend fun shutdown() {
    }
}