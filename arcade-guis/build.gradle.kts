plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)
    api(projects.arcadeEventRegistry)
    api(projects.arcadeEventsServer)
    api(projects.arcadeExtensions)

    include(api(libs.sgui.get())!!)
}
