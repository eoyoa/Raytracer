import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
        YarnLockMismatchReport.WARNING // NONE | FAIL
    rootProject.the<YarnRootExtension>().reportNewYarnLock = false // true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = false // true
}

plugins {//#plugins# apply plugins; already loaded in master project
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

val buildDir = File(project.parent?.projectDir ?: project.projectDir, "build")
if(!buildDir.exists())
  buildDir.mkdir()//$ create build folder
project.layout.buildDirectory.fileValue( File(buildDir, "client"))
val webDir = File(buildDir, "web")
if(!webDir.exists())
  webDir.mkdir()//$ create build/web folder

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
      }
      kotlin.srcDir("src/main/kotlin")
      kotlin.srcDir("../webglmath/src/main/kotlin")
      kotlin.srcDir("../common/src/main/kotlin")
    }
  }  
  js(IR) {
    binaries.executable()
    browser {
      commonWebpackConfig() {
        outputPath = webDir
      }
    }
  }  
}

val deployContent by tasks.registering(Copy::class) {
  from("src/main/content")//$ copy html and; media from here
  destinationDir = webDir
  include("**/*.*")    
}

val deployShaders by tasks.registering(Copy::class) {
  from("src/main/glsl")//$ copy glsl shaders; from here
  destinationDir = webDir
  include("**/*.*")    
}

val assemble by tasks.existing {
  dependsOn(deployContent)
  dependsOn(deployShaders)
}
