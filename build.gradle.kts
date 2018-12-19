//import com.github.salomonbrys.gradle.kjs.jstests.addKotlinJSTest
//import com.moowork.gradle.node.npm.NpmTask
//import com.moowork.gradle.node.task.NodeTask
import com.google.protobuf.gradle.*
import org.gradle.kotlin.dsl.provider.gradleKotlinDslOf
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

//import org.jetbrains.kotlin.incremental.isJavaFile
//import org.jetbrains.kotlin.konan.target.CompilerOutputKind

group = "sample"
version = "0.0.1"

val kotlinMultiplatformVersion = "1.3.11"
val serializationRuntimeVersion = "0.8.2-rc13"
val coroutinesVersion = "1.0.0"
val kodeinDIVersion = "5.4.0"
val protobufPluginVersion = "0.8.6"

val kotlinVersion = extra["kotlin.version"] as String
val protobufVersion = extra["protobuf.version"] as String
val grpcVersion = extra["grpc.version"] as String

project.extensions.add("protobuf", ProtobufConfigurator(project, null))

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:+")
        classpath(kotlin("gradle-plugin", "1.3.11"))
    }
}

allprojects {
    buildscript {
        repositories {
            jcenter()
        }
    }
    repositories {
        jcenter()
    }

    configurations.all {
        resolutionStrategy {
            failOnVersionConflict()

            eachDependency {
                when (requested.group) {
                    "com.google.protobuf"  -> {
                        when(requested.name) {
                            "protobuf-java"         -> useVersion("3.6.1")
                            "protobuf-gradle-plugin"-> useVersion("0.8.7")
                        }
                    }
                    "io.grpc"               -> useVersion(grpcVersion)
                    "org.jetbrains.kotlin"  -> useVersion(kotlinVersion)
                    "org.slf4j"             -> useVersion("1.7.25")
                    "ch.qos.logback"        -> useVersion("1.2.3")
                    "com.google.guava"      ->
                        when(requested.name) {
                            "guava"         -> useVersion("27.0.1-jre")
                            "failureaccess" -> useVersion("1.0.1")
                        }
                    "org.codehaus.plexus"   -> {
                        when(requested.name) {
                            "plexus-utils"        -> useVersion("3.0.17")
//                            "plexus-component-annotations"  -> useVersion("3.0.17")
                        }
                    }
                }
            }
        }
    }
}


apply {
    plugin<JavaPlugin>()
    plugin<ProtobufPlugin>()
}


dependencies {
    "compile"("com.google.protobuf:protobuf-java")
    "compile"("io.grpc:grpc-stub")
    "compile"("io.grpc:grpc-protobuf")

    if (JavaVersion.current().isJava9Compatible) {
        "implementation"("javax.annotation:javax.annotation-api:+")
    }
}


plugins {
    idea
    java
//    kotlin("jvm") version "1.3.11"
    id("kotlin-multiplatform") version "1.3.11"
    id("com.google.protobuf") version "0.8.7"
    "com.android.application"
    "kotlin-android"
//    id("com.android.library")
//    id("kotlinx-serialization")
//    id("com.github.salomonbrys.gradle.kjs.js-tests")
//    id("org.junit.platform.gradle.plugin")
}


repositories {
    mavenCentral()
    jcenter()
    google()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://dl.bintray.com/soywiz/soywiz")
    maven("https://dl.bintray.com/kmulti/kmulti-bignumber")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://jitpack.io")
    maven("https://plugins.gradle.org/m2/")
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

configure<ProtobufConfigurator> {
    generatedFilesBaseDir = "$projectDir/src/commonMain/generated/proto/"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}


val devHost: String? by project

kotlin {
    targets.add(presets["jvm"].createTarget("jvm"))
    targets.add(presets["js"].createTarget("nodejs").apply {
       (tasks[compilations["main"].compileKotlinTaskName] as Kotlin2JsCompile).kotlinOptions.moduleKind = "umd"
//        addKotlinJSTest()
    })
    targets.add(presets["js"].createTarget("webjs").apply {
        (tasks[compilations["main"].compileKotlinTaskName] as Kotlin2JsCompile).kotlinOptions.moduleKind = "umd"
    })
    targets.add(presets["linuxX64"].createTarget("linux"))
    targets.add(presets["macosX64"].createTarget("macos"))
    targets.add(presets["iosX64"].createTarget("iosSim").apply {
        (compilations["main"] as KotlinNativeCompilation).outputKind(NativeOutputKind.FRAMEWORK)
    })
//    targets. add(presets["android"].createTarget("android"))


    val runtimeVersion = serializationRuntimeVersion + if (devHost != "macos") "-local" else ""

    sourceSets.apply {
            val commonMain = getByName("commonMain") {
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-common");
                    implementation("com.soywiz:klock:1.0.0")
//                implementation("io.github.microutils:kotlin-logging-common:1.6.22")
//                    implementation("io.github.kmulti:kmulti-bignumber-common")
                    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")


                    implementation("com.google.protobuf:protobuf-gradle-plugin:+")
                    implementation("com.google.protobuf:protobuf-java:+")
                    implementation("io.grpc:grpc-stub:1.15.1")
                    implementation("io.grpc:grpc-protobuf:1.15.1")
                    if (JavaVersion.current().isJava9Compatible) {
                        // Workaround for @javax.annotation.Generated
                        // see: https://github.com/grpc/grpc-java/issues/3633
                        implementation("javax.annotation:javax.annotation-api:1.3.1")
                    }
                }
            }

            val commonTest = getByName("commonTest") {
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-test-common")
                    implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                }
            }

        val allJvmMain = create("allJvmMain") {
            //            kotlinOptions.jvmTarget= 1.8
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//                implementation("io.github.microutils:kotlin-logging:1.6.22")
                implementation("io.github.kmulti:kmulti-bignumber-jvm:1.2.41.2")
            }
        }

        configure(listOf(getByName("jvmMain")/*, getByName("androidMain")*/)) {
            dependsOn(allJvmMain)
        }

            val allJvmTest = create("allJvmTest") {
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-test")
                }
            }

        configure(listOf(getByName("jvmTest")/*, getByName("androidTest")*/)) {
            dependsOn(allJvmTest)

            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
            }
        }

            val allJsMain = create("allJsMain") {
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
//                implementation("io.github.microutils:kotlin-logging-js):1.6.22")
                    implementation("io.github.kmulti:kmulti-bignumber-js:1.2.41.1")
                }
            }

            configure(listOf(getByName("nodejsMain"), getByName("webjsMain"))) {
                dependsOn(allJsMain)
            }

            val allJsTest = create("allJsTest") {
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-test-js")
                }
            }

            configure(listOf(getByName("nodejsTest"), getByName("webjsTest"))) {
                dependsOn(allJsTest)
            }

            val allNativeMain = create("allNativeMain") {
                dependsOn(commonMain)
                dependencies {
                    //                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$runtimeVersion")
//                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")
                }
            }

            getByName("iosSimMain") {
                dependsOn(allNativeMain)
            }

            val allNativeDesktopMain = (devHost?.let { getByName("${it}Main") } ?: create("allNativeDesktopMain")).apply {
                dependsOn(allNativeMain)
                kotlin.srcDir("src/allNativeDesktopMain/kotlin")
            }

            configure(listOf(getByName("linuxMain"), getByName("macosMain")) - allNativeDesktopMain) {
                dependsOn(allNativeDesktopMain)
            }

            val allNativeDesktopTest = (devHost?.let { getByName("${it}Test") } ?: create("allNativeDesktopTest")).apply {
                dependsOn(commonTest)
                kotlin.srcDir("src/allNativeDesktopTest/kotlin")
            }

            configure(listOf(getByName("linuxTest"), getByName("macosTest")) - allNativeDesktopTest) {
                dependsOn(allNativeDesktopTest)
            }

    }

}





configure<ProtobufConvention> {
    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:3.6.1"
        }
        plugins {
            "kroto" {
                artifact = "com.github.marcoferrer.krotoplus:protoc-gen-kroto-plus"
            }
//            grpc {
//                artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
//            }
//            grpc-kotlin {
//                artifact = "io.rouz:grpc-kotlin-gen:0.1.0:jdk8@jar"
//            }
        }
        generateProtoTasks {
            val krotoConfig = "krotoPlusConfig.json"
            ofSourceSet("commonMain").forEach {
                it.inputs.files(krotoConfig)
                it.plugins {
                    "kroto" {
                        outputSubDir = "java"
                        option("ConfigPath=$krotoConfig")
                    }
                }
            }
        }
    }
}


fun <T : KotlinTarget> NamedDomainObjectCollection<KotlinTarget>.fromPreset(preset: KotlinTargetPreset<T>, name: String, configureAction: T.() -> Unit = {}): T {
    val target = preset.createTarget(name)
    add(target)
    target.run(configureAction)
    return target
}


tasks["build"].dependsOn(tasks.withType(GenerateProtoTask::class.java))