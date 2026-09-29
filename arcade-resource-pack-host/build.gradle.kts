plugins {
    id("arcade.common-conventions")
}

dependencies {
    api(projects.arcadeUtils)

    implementation(projects.arcadeInterceptor)
}
