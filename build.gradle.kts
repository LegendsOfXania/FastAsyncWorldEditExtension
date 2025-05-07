
plugins {
    kotlin("jvm") version "2.0.21"
    id("com.typewritermc.module-plugin") version "1.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "fr.xania"
version = "0.8.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    implementation(platform("com.intellectualsites.bom:bom-newest:1.52"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
}

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
        engineVersion = "0.8.0-beta-158"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA

        paper {
            dependency("FastAsyncWorldEdit")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier.set("")
}