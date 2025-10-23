import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.1.0"
    id("org.ajoberstar.grgit") version "4.1.1"
    id("com.gradleup.shadow") version "9.0.0-beta6"
}

var currentBranch: String = "master"/*grgit.branch.current().name
if (currentBranch != "master") {
    println("Starting in development mode")
}*/

allprojects {
    group = "lol.arch.duels"
    version = "1.7.0"

    repositories {
        configureScalaRepository()
        configureScalaRepository(dev = true)

        mavenCentral()
        maven {
            name = "lunarclient"
            url = uri("https://repo.lunarclient.dev")
        }
        maven {
            url = uri("https://repo.dmulloy2.net/repository/public/")
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "maven-publish")

    dependencies {
        compileOnly(kotlin("stdlib"))

        val commonsVersion = property("commonsVersion")
        kapt("gg.scala.commons:bukkit:$commonsVersion")
        compileOnly("gg.scala.commons:bukkit:$commonsVersion")

        compileOnly("gg.scala.store:spigot:1.0.0")
        compileOnly("gg.scala.staff:scala-scstaff-plugin:1.1.1")
        compileOnly("gg.scala.friends:scala-friends:2.1.2")
        compileOnly("gg.scala.basics.plugin:scala-basics-plugin:1.2.0")
        compileOnly("gg.tropic.game.extensions:tropic-core-game-extensions:1.8.0")

        compileOnly("gg.scala.spigot:server:1.1.3")
        compileOnly("com.lunarclient:apollo-api:1.1.8")
        compileOnly("gg.scala.lemon:bukkit:2.2.6")
        compileOnly("gg.scala.cloudsync:spigot:1.0.4")
    }

    kapt {
        keepJavacAnnotationProcessors = true
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.fork()
        options.encoding = "UTF-8"
    }

    kotlin {
        jvmToolchain(jdkVersion = 21)
    }

    tasks {
        withType<ShadowJar> {
            relocate("org.joda.time", "gg.tropic.practice.datetime")

            archiveClassifier.set("")
            archiveFileName.set(
                "Duels-${project.name}.jar"
            )
        }

        withType<JavaCompile> {
            options.compilerArgs.add("-parameters")
            options.fork()
            options.encoding = "UTF-8"
        }

        withType<KotlinCompile> {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_21
                javaParameters = true
                freeCompilerArgs.add("-Xcontext-receivers")
            }
        }

        getByName("build") {
            dependsOn(
                "shadowJar",
                "publishMavenJavaPublicationToScalaRepository"
            )
        }
    }

    publishing {
        repositories.configureScalaRepository(dev = currentBranch != "master")

        publications {
            register(
                name = "mavenJava",
                type = MavenPublication::class,
                configurationAction = shadow::component
            )
        }
    }
}

fun RepositoryHandler.configureScalaRepository(dev: Boolean = false)
{
    maven("${property("artifactory_contextUrl")}/gradle-${if (dev) "dev" else "release"}") {
        name = "scala"
        credentials {
            username = property("artifactory_user").toString()
            password = property("artifactory_password").toString()
        }
    }
}
