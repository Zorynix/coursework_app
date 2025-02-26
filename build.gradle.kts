// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    kotlin("jvm") version "2.1.10"
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    id("com.google.dagger.hilt.android") version "2.55" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.21" apply false
    id("io.gitlab.arturbosch.detekt") version("1.23.8")
    id("com.google.gms.google-services") version "4.4.2" apply false
}

detekt {
    toolVersion = "1.23.8"
    config.setFrom(file(".detekt.yml"))
    parallel=true
    source.setFrom(
        "app/src/main/java",
        "app/src/customer/kotlin",
        "app/src/restaurant/kotlin",
        "app/src/rider/kotlin")
    allRules=true
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}