import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val sentinelaLocalProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun sentinelaLocalProperty(
    name: String,
    defaultValue: String = "",
): String = sentinelaLocalProperties.getProperty(name, defaultValue)

fun buildConfigString(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val sentinelaDvrRtspPort = sentinelaLocalProperty(
    name = "sentinela.dvr.rtspPort",
    defaultValue = "554",
).toIntOrNull()?.toString() ?: "554"

android {
    namespace = "com.sentinela.camtv"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sentinela.camtv"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SENTINELA_DVR_HOST",
            buildConfigString(sentinelaLocalProperty("sentinela.dvr.host")),
        )
        buildConfigField(
            "String",
            "SENTINELA_DVR_USERNAME",
            buildConfigString(sentinelaLocalProperty("sentinela.dvr.username")),
        )
        buildConfigField(
            "String",
            "SENTINELA_DVR_PASSWORD",
            buildConfigString(sentinelaLocalProperty("sentinela.dvr.password")),
        )
        buildConfigField(
            "int",
            "SENTINELA_DVR_RTSP_PORT",
            sentinelaDvrRtspPort,
        )
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation("androidx.media3:media3-exoplayer:1.10.1")
    implementation("androidx.media3:media3-exoplayer-rtsp:1.10.1")
    implementation("androidx.media3:media3-ui:1.10.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
