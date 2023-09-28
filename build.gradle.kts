plugins {
    kotlin("jvm") version "1.8.21"
    `maven-publish`
}

group = "be.solidlab"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.jena:jena-shacl:4.7.0")
    implementation("com.graphql-java:graphql-java:20.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

publishing {
    publications {
        create<MavenPublication>("shapeshift") {
            groupId = "be.solidlab"
            artifactId = "shapeshift"
            version = "0.1"
            from(components["kotlin"])
        }
    }

}