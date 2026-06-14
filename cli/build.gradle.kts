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
    implementation(project(":core"))

    implementation("dev.tamboui:tamboui-core:0.3.0")
    implementation("dev.tamboui:tamboui-tui:0.3.0")
    implementation("dev.tamboui:tamboui-jline3-backend:0.3.0")
    implementation("dev.tamboui:tamboui-toolkit:0.3.0")

    implementation("cn.hutool:hutool-all:5.8.38")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("junit:junit:4.13.1")
}

tasks.register<Jar>("fatJar") {
    group = "build"
    manifest {
        attributes["Main-Class"] = "org.a8043.simpleCode.cli.Main"
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
    archiveClassifier.set("all")
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.register<Exec>("run") {
    dependsOn(tasks.named("fatJar"))
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
