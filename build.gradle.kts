plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.ploceus)
}

ploceus.setIntermediaryGeneration(2)

group = "org.polyfrost"
version = libs.versions.waveycapes.get()

repositories {
    mavenCentral()
    maven("https://maven.cloverclient.com/releases")
    maven("https://maven.legacyfabric.net/")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.legacy.yarn)

    modImplementation(libs.devauth.fabric)
    modImplementation(libs.fabric.loader)

    ploceus.dependOsl(libs.versions.osl.get())
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching(listOf("fabric.mod.json")) {
        expand(mapOf("version" to version))
    }
}

kotlin {
    jvmToolchain(25)
}

loom {
    runs.named("client") {
        ideConfigGenerated(true)
        runDir("run")

        jvmArguments.add("-Ddevauth.enabled=true")
        jvmArguments.add("-Ddevauth.account=main")
    }
}