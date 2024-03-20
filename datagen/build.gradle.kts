base.archivesName.set("arcade-datagen")
version = rootProject.version

dependencies {
    implementation(project(mapOf("path" to ":", "configuration" to "namedElements")))

    include(implementation("org.apache.commons:commons-text:1.11.0")!!)
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("arcadeDatagen") {
            groupId = "com.github.CasualChampionships"
            artifactId = "arcade-datagen"
            version = "1.0.4"
            from(components["java"])
        }
    }
}