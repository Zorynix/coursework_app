plugins {
    kotlin("jvm") version "2.1.10"
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    id("com.google.dagger.hilt.android") version "2.55" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.21" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("com.google.gms.google-services") version "4.4.2" apply false
}

detekt {
    toolVersion = "1.23.8"
    config.setFrom(file("$rootDir/detekt.yml"))
    parallel = true
    buildUponDefaultConfig = true
    source.setFrom(
        "app/src/main/java",
        "app/src/customer/kotlin",
        "app/src/restaurant/kotlin",
        "app/src/rider/kotlin",
    )
    allRules = true
    autoCorrect = true
    baseline = file("$rootDir/detekt-baseline.xml")
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    ktlint {
        verbose.set(true)
        outputToConsole.set(true)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
        filter {
            exclude("**/generated/**")
            include("**/*.kt")
        }
    }
}

tasks {
    register("format") {
        description = "Formats Kotlin code using ktlint"
        group = "formatting"
        dependsOn(":ktlintFormat")
    }

    register("checkAll") {
        description = "Checks code with ktlint and Detekt"
        group = "verification"
        dependsOn(":ktlintCheck", ":detekt")
    }

    named("detekt") {
        val baselineFile = file("$rootDir/detekt-baseline.xml")
        if (!baselineFile.exists()) {
            println("Baseline file not found, generating detekt-baseline.xml...")
            dependsOn("detektBaseline")
        }
    }
}
