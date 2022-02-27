// import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kobwebx.markdown)
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
}

group = "dev.bitspittle.morple"
version = SimpleDateFormat("yyyyMMdd.kkmm").apply {
    timeZone = TimeZone.getTimeZone("UTC")
}.format(Date())

kobweb {
    appGlobals.put("version", version.toString())

    index {
        description.set("Powered by Kobweb")
    }
}

kotlin {
    /*
    jvm {
        tasks.withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "11"
        }

        tasks.named("jvmJar", Jar::class.java).configure {
            archiveFileName.set("morple.jar")
        }
    }
    */
    js(IR) {
        moduleName = "morple"
        browser {
            commonWebpackConfig {
                outputFileName = "morple.js"
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(libs.kobweb.core)
                implementation(libs.kobweb.silk.core)
                implementation(libs.kobweb.silk.icons.fa)
                implementation(libs.kobwebx.markdown)
             }
        }

        /*
        val jvmMain by getting {
            dependencies {
                implementation(libs.kobweb.api)
             }
        }
        */
    }
}