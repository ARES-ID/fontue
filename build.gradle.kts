plugins {
    alias(libs.plugins.orgJetbrainsKotlinJvm)
    alias(libs.plugins.comGradlePluginPublish)
    alias(libs.plugins.orgJmailenKotlinter)
    alias(libs.plugins.ioGitlabArturboshDetekt)
}

group = libs.versions.projectGroup.get()
version = libs.versions.projectVersion.get()

dependencies {
    implementation(libs.comSquareupOkio.okio)
    implementation(libs.ioKtor.ktorClientCore)
    implementation(libs.ioKtor.ktorClientCio)
    implementation(libs.ioKtor.ktorClientLogging)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        explicitApi()
        allWarningsAsErrors.set(true)
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.SAP)
    }
}

gradlePlugin {
    plugins {
        create("fontue") {
            id = "com.rjspies.fontue"
            implementationClass = "com.rjspies.fontue.FontuePlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "fontue"
            url = uri("../local-plugin-repository")
        }
    }
}
