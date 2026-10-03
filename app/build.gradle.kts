plugins {
    alias(libs.plugins.android.application)
    //id("de.mannodermaus.android-junit5") version "1.11.0"
}

android {
    namespace = "com.example.medialibrary"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.medialibrary"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.01"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArgument("runnerBuilder", "de.mannodermaus.junit5.AndroidJUnit5Builder")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    sourceSets {
        getByName("main") {
            // This tells Gradle to look inside both 'src/main/java' and your new 'backend' folder
            java.srcDirs("../backend")
        }
    }

    testOptions {
        animationsDisabled = true
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/LICENSE*",
            "META-INF/NOTICE*"
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.test:core:1.6.1")
        force("androidx.test:core-ktx:1.6.1")
        force("androidx.test:runner:1.6.2")
        force("androidx.test:monitor:1.7.2")
        force("androidx.test.ext:junit:1.2.1")
        force("androidx.test.espresso:espresso-core:3.6.1")
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("com.github.PhilJay.MPAndroidChart:MPChartLib:v4.0.1")

    // Pure JUnit 5 Local Testing Stack
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("de.mannodermaus.junit5:android-test-core:1.4.0")

    // Mocking Ecosystem
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockk)

    // Android Context Testing Architecture
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumentation / UI Automation Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    debugImplementation(libs.androidx.fragment.testing)

    // JUnit 5 Support for Device Tests
    androidTestImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.4.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.4.0")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    implementation(kotlin("test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}