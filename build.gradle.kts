plugins {
    /* Kotlin */
    kotlin("jvm") version "2.2.10"
    /* paperweight */
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    /* Typewriter */
    id("com.typewritermc.module-plugin") version "2.1.0"
}

repositories {
    /* FAWE */
    maven("https://maven.enginehub.org/repo/")
}


dependencies {
    /* FAWE */
    implementation(platform("com.intellectualsites.bom:bom-newest:1.52"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
    /* paperweight */
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
}


group = "fr.xania"
version = "0.9.0"

typewriter {
    namespace = "legendsofxania"

    extension {
        name = "FastAsyncWorldEdit"
        shortDescription = "Spawn FAWE schematics with Typewriter"
        description = """
            |Spawn schematics in your interactions and create
            |beautiful places directly in Typewriter.
            |Created by the Legends of Xania.
            """.trimMargin()
        engineVersion = "0.9.0-beta-167"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA

        paper {
            dependency("FastAsyncWorldEdit")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
