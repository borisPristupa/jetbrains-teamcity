plugins {
    kotlin("jvm") version "1.3.61"
    application.apply(true)
}

group = "com.boris.internship"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit:junit:4.13")

    implementation(kotlin("stdlib-jdk8"))

    compile("org.apache.httpcomponents:httpclient:4.5.12")
    compile("org.apache.httpcomponents:httpmime:4.5.12")
    compile("com.fasterxml.jackson.core:jackson-databind:2.0.1")

    compile ("log4j:log4j:1.2.17")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

application {
    mainClassName = "com.boris.internship.MainKt"
}
