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

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("junit:junit:4.13.1")
}

val nativeDir = buildDir.resolve("native")

tasks.test {
    useJUnit()
    workingDir = rootProject.file("test")
    systemProperty("java.library.path", nativeDir.absolutePath)
}

tasks.register<Exec>("generateNativeHeader") {
    outputs.cacheIf { true }

    val javaFile = file("src/main/java/org/a8043/simpleCode/tools/windows/Native.java")
    inputs.file(javaFile)

    val headerDir = file("src/main/cpp")
    val headerClassesDir = buildDir.resolve("generated/native-header-classes")
    outputs.file(headerDir.resolve("org_a8043_simpleCode_tools_windows_Native.h"))
    outputs.dir(headerClassesDir)

    commandLine(
        "javac",
        "-h", headerDir.absolutePath,
        "-d", headerClassesDir.absolutePath,
        "-cp", sourceSets.main.get().compileClasspath.asPath,
        javaFile.absolutePath
    )

    doFirst {
        headerDir.mkdirs()
    }
}

tasks.register<Exec>("buildNative") {
    outputs.cacheIf { true }

    val dll = nativeDir.resolve("native.dll")
    outputs.file(dll)

    val cpp = file("./src/main/cpp/native.cpp")
    inputs.file(cpp)

    val javaHome = System.getenv("JAVA_HOME") ?: error("JAVA_HOME is not set")
    inputs.property("javaHome", javaHome)

    commandLine(
        "g++", "-shared", "-fPIC", "-O2",
        "-I", "$javaHome/include",
        "-I", "$javaHome/include/win32",
        "-o", dll.absolutePath,
        cpp.absolutePath,
        "-luiautomationcore", "-lole32", "-loleaut32", "-luuid"
    )

    doFirst {
        nativeDir.mkdirs()
    }
}

tasks.register<Copy>("copyNative") {
    dependsOn("buildNative")
    from("$nativeDir")
    into("${project(":cli").buildDir}/libs")
}
