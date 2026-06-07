import gradle.kotlin.dsl.accessors._cc74bba6df91495c780495629742604f.annotationProcessor
import gradle.kotlin.dsl.accessors._cc74bba6df91495c780495629742604f.compileOnly
import gradle.kotlin.dsl.accessors._cc74bba6df91495c780495629742604f.main
import gradle.kotlin.dsl.accessors._cc74bba6df91495c780495629742604f.sourceSets
import gradle.kotlin.dsl.accessors._cc74bba6df91495c780495629742604f.testAnnotationProcessor
import gradle.kotlin.dsl.accessors._cc74bba6df91495c780495629742604f.testCompileOnly
import gradle.kotlin.dsl.accessors._cc74bba6df91495c780495629742604f.testImplementation
import gradle.kotlin.dsl.accessors._cc74bba6df91495c780495629742604f.testRuntimeOnly
import info.preva1l.advancedserverzones.BuildConstants
import info.preva1l.trashcan.finallyADecent
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
    java
    id("asz.publishing")
    id("info.preva1l.trashcan")
    id("com.gradleup.shadow")
}

repositories {
    maven("https://reposilite.slne.dev/public")
//    finallyADecent(dev = BuildConstants.DEV_MODE)
//    finallyADecent()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    compileOnly("org.jetbrains:annotations:26.0.2")
    annotationProcessor("org.jetbrains:annotations:26.0.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
    testCompileOnly("org.jetbrains:annotations:26.0.2")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
}

configurations.all {
    resolutionStrategy.capabilitiesResolution.withCapability("org.bukkit:bukkit") {
        select("io.papermc.paper:paper-api:0")
    }
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        destinationDirectory.set(file("$rootDir/target"))
        archiveFileName.set("${rootProject.name}-${project.name.uppercaseFirstChar()}-$version.jar")
    }

    withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.encoding = "UTF-8"
        options.release = 25
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    withType<Javadoc> {
        (options as StandardJavadocDocletOptions).tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }

    register<Jar>("javadocJar") {
        dependsOn("javadoc")
        archiveClassifier.set("javadoc")
        from(named<Javadoc>("javadoc").get().destinationDir)
    }

    withType<Test> {
        useJUnitPlatform()
    }

    named("build") {
        dependsOn("shadowJar")
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
        }
    }
}
