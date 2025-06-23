loom {
    accessWidenerPath.set(file("src/main/resources/arcade-utils.accesswidener"))
}

dependencies {
    include(modApi(libs.fix.codec.order.get())!!)
}