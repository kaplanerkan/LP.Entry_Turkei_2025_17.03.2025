import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //id("com.google.gms.google-services")
    //id("com.google.firebase.crashlytics")
}



android {
    namespace = "com.eqpos.eqentry"
    compileSdk = 36

    signingConfigs {
        create("release") {
            val isWindows =
                System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")
            storeFile = file(
                if (isWindows) "C:\\Users\\Lotus\\Desktop\\Projecte\\sign.jks"
                else "/home/erkan/Downloads/Warpinator/sign.jks"
            )

            keyAlias = "lpmobile"
            keyPassword = "gigabyte55"
            storePassword = "gigabyte55"
        }
    }


    //  project.archivesBaseName = "LPEntry";

    defaultConfig {
        applicationId = "com.eqpos.eqentry"
        minSdk = 24
        targetSdk = 36
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        versionCode = 4
        versionName = "1.3.30"
        versionNameSuffix = "_tr"
    }

    // APK dosya adını özelleştirme
    applicationVariants.configureEach {
        outputs.configureEach {
            setProperty("archivesBaseName", "LPEntry-v${versionCode}-${versionName}_tr")
        }
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "21"
    }
}


dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    //implementation ("com.android.support.constraint:constraint-layout:1.0.2")
    //implementation ("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation(libs.constraintlayout)

    //implementation ("com.google.code.gson:gson:2.8.1")
    implementation(libs.gson)

    //implementation ("com.google.android.material:material:1.12.0")
    implementation(libs.material)

    //implementation ("androidx.activity:activity:1.10.1")
    implementation(libs.activity)
    implementation(libs.recyclerview)

    testImplementation(libs.junit)
//    androidTestImplementation 'com.android.support.test:runner:1.0.1'
//    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.1'

    //implementation 'com.journeyapps:zxing-android-embedded:3.2.0@aar'
    //implementation 'libs/zxing-android-embedded:3.2.0@aar'


    implementation(libs.core)

    //implementation 'com.baoyz.swipemenulistview:library:1.3.0'   //  library-1.3.0.aar

    //noinspection GradleCompatible
    //implementation 'com.android.support:appcompat-v7:23.3.0'

    //implementation ("androidx.appcompat:appcompat:1.0.0")
    implementation(libs.appcompat)

    //noinspection GradleCompatible
    //implementation ("com.android.support:design:23.3.0")

    //implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    //implementation 'com.google.firebase:firebase-analytics-ktx'
    //implementation 'com.google.firebase:firebase-crashlytics'

    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)
}
