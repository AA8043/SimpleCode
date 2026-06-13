plugins {
    java
    id("io.franzbecker.gradle-lombok") version "3.0.0"
}

group = "org.a8043.simpleCode"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("cn.hutool:hutool-all:5.8.38")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("junit:junit:4.13.1")
}

tasks.test {
    useJUnit()
    workingDir = project.rootProject.file("test")
}
