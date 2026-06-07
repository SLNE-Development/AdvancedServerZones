plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://reposilite.slne.dev/public")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.1.0")
    implementation("org.ajoberstar.grgit:org.ajoberstar.grgit.gradle.plugin:5.3.2")
    implementation("info.preva1l.trashcan:Trashcan-Tooling:1.0.4") {
        exclude(group = "io.papermc.paperweight.userdev", module = "io.papermc.paperweight.userdev.gradle.plugin")
    }
}