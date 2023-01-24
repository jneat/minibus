val artifactGroup = "com.github.jneat"
plugins {
    id("java")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.+")

    testImplementation("org.assertj:assertj-core:3.24.+")
    testImplementation("org.testng:testng:7.7.1")
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    test {
        useTestNG()
        testLogging.showStandardStreams = true
    }

    val sourcesJar by creating(Jar::class) {
        dependsOn(classes)
        classifier = "sources"
        from(sourceSets["main"].allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn(javadoc)
        classifier = "javadoc"
        from(javadoc.get().destinationDir)
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", javadocJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("projectMvnPublication") {
            version = version
            groupId = artifactGroup
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}