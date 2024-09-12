base.archivesName.set("arcade-datagen")

val modVersion = "1.0.15"
version = "${modVersion}+mc${libs.versions.minecraft.get()}"

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

    register("runDatagenClient") {
        group = "fabric"
        runClient.get().args("--arcade-datagen")
        dependsOn(runClient)
    }
}

publishing {
    publications {
        create<MavenPublication>("arcadeDatagen") {
            groupId = "net.casual-championships"
            artifactId = "arcade-datagen"
            version = "${modVersion}+${libs.versions.minecraft.get()}"
            from(components["java"])
        }
    }
}