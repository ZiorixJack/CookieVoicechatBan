plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.cookievcban"
version = "1.0.0"
description = "Voice chat ban plugin for Paper 1.21.5"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.maxhenkel.de/repository/public")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    
    // SimpleVoiceChat API
    compileOnly("de.maxhenkel.voicechat:voicechat-api:2.5.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }
    
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to "1.21"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
