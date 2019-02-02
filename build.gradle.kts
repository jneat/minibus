import com.jfrog.bintray.gradle.BintrayExtension

val artifactGroup = "com.github.jneat"
val artifactVersion = "0.5.0"
plugins {
    id("java")
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.4"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile("org.slf4j:slf4j-api:1.7.+")

    testCompile("org.assertj:assertj-core:3.9.0")
    testCompile("org.testng:testng:6.14.2")
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
            version = artifactVersion
            groupId = artifactGroup
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    setPublications("projectMvnPublication")
    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "jneat"
        name = "minibus"
        userOrg = System.getenv("BINTRAY_USER")
    })
}
