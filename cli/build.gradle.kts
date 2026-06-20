plugins {
    java
    id("io.franzbecker.gradle-lombok") version "3.0.0"
    id("com.gradleup.shadow") version "9.4.2"
}

group = "org.a8043.simpleCode"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))

    implementation("org.slf4j:slf4j-api:2.0.18")
    implementation("org.apache.logging.log4j:log4j-core:2.26.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.26.0")

    implementation("dev.tamboui:tamboui-core:0.3.0")
    implementation("dev.tamboui:tamboui-tui:0.3.0")
    implementation("dev.tamboui:tamboui-jline3-backend:0.3.0")
    implementation("dev.tamboui:tamboui-toolkit:0.3.0")

    implementation("cn.hutool:hutool-all:5.8.38")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("junit:junit:4.13.1")
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "org.a8043.simpleCode.cli.Main"
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
    archiveClassifier.set("all")
    mergeServiceFiles()
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.register<Exec>("run") {
    dependsOn(tasks.named("shadowJar"))
    workingDir = project.rootProject.file("test")

    var prefix = listOf("")
    if (System.getProperty("os.name").lowercase().contains("win")) {
        prefix = listOf("cmd", "/c", "start", "cmd", "/c")
    } else {
        prefix = listOf("sh", "-c")
    }

    commandLine(
        *prefix.toTypedArray(), System.getProperty("java.home") + "/bin/java", "-jar",
        "${buildDir}/libs/${project.name}-${project.version}-all.jar"
    )
}
