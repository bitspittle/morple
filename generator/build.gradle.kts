plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "dev.bitspittle.morple.generator"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))
}

application {
    mainClass.set("MainKt")
}