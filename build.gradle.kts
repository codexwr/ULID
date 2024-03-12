import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
    `java-library`
}

val javaVersion = JavaVersion.VERSION_1_8
java.sourceCompatibility = javaVersion

group = "com.github.codexwr"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xjsr305=strict", "-Xjvm-default=all", "-Xemit-jvm-type-annotations")
        jvmTarget = javaVersion.toString()
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(javaVersion.majorVersion.toInt())
}

java {
    withSourcesJar()
}

tasks.jar {
    manifest.attributes.also {
        it["Implementation-Title"] = project.name
        it["Implementation-Version"] = project.version
    }
}

val sourceJar by tasks.registering(Jar::class) {
//    dependsOn("classes")
    from(sourceSets.main.get().allSource)

    archiveClassifier.set("sources")
    manifest.attributes.also {
        it["Implementation-Title"] = project.name
        it["Implementation-Version"] = project.version
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name.lowercase()
            version = project.version.toString()

            from(components["java"])
//            artifact(sourceJar)

            pom {
                name = project.name
                description = "ULID for Kotlin JVM Project"
                url = "https://github.com/codexwr/ulid"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "codexwr"
                        name = "codexwr"
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages" //GitHub Package Repository
            url = uri(System.getenv("GPR_URL") ?: project.findProperty("gpr.url") as String)
            credentials {
                username = System.getenv("GPR_USERNAME") ?: project.findProperty("gpr.username") as String
                password = System.getenv("GPR_PASSWORD") ?: project.findProperty("gpr.password") as String
            }
        }
    }
}
