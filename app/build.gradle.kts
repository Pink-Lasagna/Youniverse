import com.android.build.gradle.internal.tasks.R8Task
import com.android.ide.common.resources.MergedResourceWriter

plugins {
    id("com.android.application")
}

android {
    namespace = "ru.jaromirchernyavsky.youniverse"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.jaromirchernyavsky.youniverse"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isCrunchPngs = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}
dependencies {
    implementation("commons-io:commons-io:2.16.1")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.apache.commons:commons-imaging:1.0.0-alpha5")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.yanzhenjie.recyclerview:x:1.3.2")
    implementation("ar.com.hjg:pngj:2.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.leandroborgesferreira:loading-button-android:2.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-text
    implementation("org.apache.commons:commons-text:1.12.0")

    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}