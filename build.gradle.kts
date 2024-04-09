import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "1.9.23"
}

group = "org.types"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmMain.get().dependsOn(commonMain.get())
        mingwMain.get().dependsOn(commonMain.get())

        jvmTest.get().dependsOn(commonTest.get())
    }
}