plugins {
    java
    id("io.franzbecker.gradle-lombok") version "3.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))

    implementation("org.slf4j:slf4j-api:2.0.18")
    implementation("org.apache.logging.log4j:log4j-core:2.26.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.26.0")

    implementation("cn.hutool:hutool-all:5.8.38")
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
    implementation("javax.mail:mail:1.4.7")

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
