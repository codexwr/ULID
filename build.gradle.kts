import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

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
        jvmTarget = "1.8"
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
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
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name.lowercase()
            version = project.version.toString()

            from(components["java"])
            artifact(sourceJar)
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        }
    }
}