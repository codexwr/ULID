import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
}

val javaVersion = JavaVersion.VERSION_1_8
java.sourceCompatibility = javaVersion

group = "com.chans.codexwr"
version = "1.0.0"

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
            artifact(sourceJar)
        }
    }
    repositories {
        maven {
            name = "GitHubPackages" //GitHub Package Repository
            url = uri(project.findProperty("gpr.url") as? String? ?: System.getenv("GPR_URL"))
            credentials {
                username = project.findProperty("gpr.username") as? String? ?: System.getenv("GPR_USERNAME")
                password = project.findProperty("gpr.password") as? String? ?: System.getenv("GPR_PASSWORD")
            }
        }
    }
}