import info.preva1l.trashcan.paper

plugins {
    asz.common
}

dependencies {
    paper("26.1.2.build.+")
}

tasks.register("publishApi") {
    dependsOn("publishMavenJavaPublicationToFinallyADecentRepository")
}