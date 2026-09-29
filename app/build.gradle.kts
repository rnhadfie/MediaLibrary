plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.medialibrary"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.medialibrary"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions {
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

// 🟩 CRUCIAL STEP FOR JUNIT 5: Instruct Gradle's unit test runner to use JUnit Platform
tasks.withType<Test> {
    useJUnitPlatform()
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

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("io.mockk:mockk:1.14.11")


    implementation(project(":MPChartLib"))


    // ADD THIS LINE to allow JUnit 5 platform to run JUnit 4 tests
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.0")

    // Mockito and Testing ecosystem
    testImplementation(libs.mockito.core)
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.0")


    // Note: mockito-inline is deprecated/built-in starting from Mockito 5.x.
    // If you run into issues mock-stubbing final classes, change this to mockito-subclass or remove it.
    testImplementation("org.mockito:mockito-inline:5.2.0")

    testImplementation("org.robolectric:robolectric:4.12.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // Vintage Engine (Allows you to run your legacy JUnit 4 tests side-by-side with JUnit 5)
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
    implementation(kotlin("test"))
}