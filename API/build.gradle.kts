import info.preva1l.trashcan.paper

plugins {
    asz.common
}

dependencies {
    paper("1.21.8-R0.1-SNAPSHOT")
}

tasks.register("publishApi") {
    dependsOn("publishMavenJavaPublicationToFinallyADecentRepository")
}