
plugins {
    kotlin("jvm") version "2.0.21"
    id("java-library")
    id("com.typewritermc.module-plugin") version "1.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "fr.xania"
version = "0.8.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("net.sandrohc:schematic4j:1.1.0")
}

typewriter {
    namespace = "legendsofxania"

    extension {
        name = "Schematics"
        shortDescription = "Spawn schematics with Typewriter"
        description = """
            |Spawn schematics in your interactions and create
            |beautiful places directly in Typewriter.
            |Created by the Legends of Xania.
            """.trimMargin()
        engineVersion = "0.8.0-beta-158"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA

        paper {}
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    mergeServiceFiles()
}
