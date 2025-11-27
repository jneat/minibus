val artifactGroup = "io.github.jneat"

plugins {
    id("java")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.+")

    testImplementation("org.assertj:assertj-core:3.27.+")
    testImplementation("org.testng:testng:7.11.+")
}

tasks {
    java {
        withJavadocJar()
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    test {
        useTestNG()
        testLogging.showStandardStreams = true
    }

    javadoc {
        options.overview = "src/main/resources/overview.md"
    }
}

publishing {
    publications {
        create<MavenPublication>("projectMvnPublication") {
            version = version
            groupId = artifactGroup
            from(components["java"])
        }
    }
}