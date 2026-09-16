plugins {
    id("java-library")
    id("com.gtnewhorizons.retrofuturagradle") version "2.0.2"
}

// Project properties
group = "com.fkcac"
// CI 通过 -PmodVersion=... 覆盖；本地默认为 1.1.0
version = project.findProperty("modVersion") as? String ?: "1.1.0"

// Decouple the JVM Gradle runs under from the one used to compile / run the mod (1.7.10 needs Java 8)
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        // Azul covers the most platforms for Java 8 toolchains, including macOS arm64
        vendor.set(org.gradle.jvm.toolchain.JvmVendorSpec.AZUL)
    }
}

// Most RetroFuturaGradle configuration lives here
minecraft {
    mcVersion.set("1.7.10")

    // Username for client run configurations
    username.set("Developer")

    // Generate a field named VERSION in the injected Tags class with the mod version
    injectedTags.put("VERSION", project.version)
}

// Generates a class named com.fkcac.Tags with the mod version in it
tasks.injectTags.configure {
    outputClassName.set("${project.group}.Tags")
}

// Put the version from gradle into mcmod.info
tasks.processResources.configure {
    val projVersion = project.version.toString() // Needed for configuration cache to work
    inputs.property("version", projVersion)

    filesMatching("mcmod.info") {
        expand(mapOf("modVersion" to projVersion))
    }
}

// Dependency repositories
repositories {
    maven {
        name = "OvermindDL1 Maven"
        url = uri("https://gregtech.overminddl1.com/")
    }
    maven {
        name = "GTNH Maven"
        url = uri("https://nexus.gtnewhorizons.com/repository/public/")
    }
}

dependencies {
    // Runtime dependencies that exist on the server side (plugin messaging protocol) are not needed on the classpath.
    // Any mod jar dependency (deobfuscated) can be added like:
    // api(rfg.deobf("curse.maven:<slug>-<projectid>:<fileid>"))
}