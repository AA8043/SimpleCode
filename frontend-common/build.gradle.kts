plugins {
    java
    id("io.freefair.lombok") version "8.6"
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
}

tasks.test {
    useJUnit()
    workingDir = rootProject.file("test")
}
