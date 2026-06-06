plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)   // Plugin de Firebase
    alias(libs.plugins.kotlin.parcelize)   // Plugin para pasar objetos entre pantallas si lo requieren
}

android {
    namespace = "com.example.gamehealthmanager" // Asegúrate de que coincida con tu paquete local
    compileSdk = 34

    defaultConfig {
        // ID solicitado por el profesor si estás en la de Banca, o mantén el tuyo si es GameVault
        applicationId = "com.curso.banca"
        minSdk = 26
        targetSdk = 34
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding {
        enable = true
    }
}

dependencies {
    // Componentes base y diseño
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)

    // =======================================================================
    // SOLUCIÓN AL ERROR: Navegación Jetpack (Mapeado exacto de tu TOML)
    // =======================================================================
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Consumo de APIs (Retrofit y OkHttp con la nomenclatura limpia)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging) // <- Asegúrate de que use puntos '.' en lugar de '-'

    // Concurrencia (Corrutinas)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Diseños y Animaciones
    implementation(libs.lottie)
    implementation(libs.glide)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}