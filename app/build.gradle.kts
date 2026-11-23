import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
val tieneKeystoreRelease = keystorePropertiesFile.exists().also {
    if (it) keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "cl.duoc.mpazneb"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.mpazneb"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Suite H6 aislada: no ejecutar pruebas que requieren backend real (p. ej. SaludApi de Martín).
        testInstrumentationRunnerArguments["notAnnotation"] =
            "cl.duoc.mpazneb.IntegracionBackendReal"

        // Release: sin API pública del equipo; las llamadas remotas no están disponibles.
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"https://sin-servidor-configurado.invalid/\""
        )
    }

    signingConfigs {
        create("release") {
            if (tieneKeystoreRelease) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            // Emulador + adb reverse tcp:8080 tcp:8080 → Spring Boot en el PC.
            buildConfigField("String", "API_BASE_URL", "\"http://127.0.0.1:8080/\"")
        }
        release {
            optimization {
                enable = false
            }
            if (tieneKeystoreRelease) {
                signingConfig = signingConfigs.getByName("release")
            }
            val releaseApi = (findProperty("mpazReleaseApiBaseUrl") as String?)
                ?: System.getenv("MPAZ_RELEASE_API_BASE_URL")
                ?: "https://sin-servidor-configurado.invalid/"
            buildConfigField("String", "API_BASE_URL", "\"$releaseApi\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        animationsDisabled = true
    }
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/LICENSE.md",
                "/META-INF/LICENSE-notice.md",
            )
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.retrofit.asProvider())
    implementation(libs.retrofit.converter.gson)
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

/** Cobertura IE 3.2.1: ViewModels, Repositories, domain y clientes Retrofit. */
val jacocoLogicalIncludes = listOf(
    "**/data/repository/**",
    "**/viewmodel/**",
    "**/domain/**",
    "**/data/remote/RetrofitClient*",
    "**/data/remote/openlibrary/OpenLibraryClient*",
)

val jacocoExcludes = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "**/*\$Companion",
    "**/*\$WhenMappings",
    "**/*ComposableSingletons*",
)

tasks.register<JacocoReport>("jacocoDebugUnitTestReport") {
    group = "verification"
    description = "Informe JaCoCo de pruebas unitarias debug (HTML + XML)."
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(
            layout.buildDirectory.file("reports/jacoco/jacocoDebugUnitTestReport/jacoco.xml")
        )
        html.outputLocation.set(
            layout.buildDirectory.dir("reports/jacoco/jacocoDebugUnitTestReport/html")
        )
    }

    classDirectories.setFrom(
        fileTree(
            layout.buildDirectory.dir(
                "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
            ),
        ) { exclude(jacocoExcludes) },
        fileTree(
            layout.buildDirectory.dir(
                "intermediates/javac/debug/compileDebugJavaWithJavac/classes",
            ),
        ) { exclude(jacocoExcludes) },
    )
    sourceDirectories.setFrom(
        files("src/main/java", "src/main/kotlin")
    )
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec",
            )
        }
    )
}

tasks.register<JacocoReport>("jacocoDebugLogicalUnitTestReport") {
    group = "verification"
    description = "JaCoCo del código lógico (ViewModels, Repositories, domain) — IE 3.2.1."
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(
            layout.buildDirectory.file("reports/jacoco/jacocoDebugLogicalUnitTestReport/jacoco.xml"),
        )
        html.outputLocation.set(
            layout.buildDirectory.dir("reports/jacoco/jacocoDebugLogicalUnitTestReport/html"),
        )
    }

    classDirectories.setFrom(
        fileTree(
            layout.buildDirectory.dir(
                "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
            ),
        ) {
            include(jacocoLogicalIncludes)
            exclude(jacocoExcludes)
        },
        fileTree(
            layout.buildDirectory.dir(
                "intermediates/javac/debug/compileDebugJavaWithJavac/classes",
            ),
        ) {
            include(jacocoLogicalIncludes)
            exclude(jacocoExcludes)
        },
    )
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec",
            )
        },
    )
}

tasks.register<JacocoReport>("jacocoDebugAndroidTestReport") {
    group = "verification"
    description = "Informe JaCoCo de pruebas instrumentadas debug (HTML + XML)."
    dependsOn("connectedDebugAndroidTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(
            layout.buildDirectory.file("reports/jacoco/jacocoDebugAndroidTestReport/jacoco.xml")
        )
        html.outputLocation.set(
            layout.buildDirectory.dir("reports/jacoco/jacocoDebugAndroidTestReport/html")
        )
    }

    classDirectories.setFrom(
        fileTree(
            layout.buildDirectory.dir(
                "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
            ),
        ) { exclude(jacocoExcludes) },
        fileTree(
            layout.buildDirectory.dir(
                "intermediates/javac/debug/compileDebugJavaWithJavac/classes",
            ),
        ) { exclude(jacocoExcludes) },
    )
    sourceDirectories.setFrom(
        files("src/main/java", "src/main/kotlin")
    )
    executionData.setFrom(
        fileTree(layout.buildDirectory.get().asFile) {
            include(
                "outputs/code_coverage/debugAndroidTest/connected/**/*.ec",
                "outputs/code_coverage/debugAndroidTest/connected/**/*.exec",
            )
        }
    )
}
