plugins {
    alias(libs.plugins.orgJetbrainsKotlinJvm)
    alias(libs.plugins.comGradlePluginPublish)
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
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.SAP)
    }
}

gradlePlugin {
    plugins {
        create("fontue") {
            id = "com.rjspies.fontue"
            implementationClass = "FontuePlugin"
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
