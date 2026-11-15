// 1. Configuración de plugins de Gradle para el módulo Android Application y Soporte para Jetpack Compose
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// 2. Configuración principal de Android Gradle Plugin (AGP)
android {
    namespace = "com.example.aplicacionmovilnpi"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.aplicacionmovilnpi"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Configuración de variantes de compilación (Release / Debug)
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Compatibilidad de bytecode Java/Kotlin
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Habilitar características de compilación (Jetpack Compose UI)
    buildFeatures {
        compose = true
    }

    // Configuración de empaquetado de librerías nativas JNI
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

// 3. Declaración de dependencias del proyecto
dependencies {
    // Jetpack Compose BOM para gestión centralizada de versiones
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // OSMDroid para representación de mapas OpenStreetMap
    implementation(libs.osmdroid.android)

    // CameraX para la captura e integración del escáner de códigos QR
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // ML Kit Barcode Scanning para el reconocimiento visual de códigos QR
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // ZXing para la generación dinámica de códigos QR
    implementation("com.google.zxing:core:3.5.2")

    // Frameworks de pruebas unitarias e instrumentales
    testImplementation(libs.junit)
    testImplementation(libs.org.json)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
