plugins {
    kotlin("jvm") version "2.0.21"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("com.typewritermc.module-plugin") version "1.3.0"
}

repositories {
    maven("https://maven.enginehub.org/repo/")
}


dependencies {
    implementation(platform("com.intellectualsites.bom:bom-newest:1.52"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
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
        engineVersion = "0.9.0-beta-162"
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
