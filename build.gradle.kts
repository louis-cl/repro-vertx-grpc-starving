import com.google.protobuf.gradle.id

plugins {
    id("java")
    id("com.google.protobuf") version "0.9.5"
}

group = "com.github.louiscl"
version = "1.0-SNAPSHOT"

val vertxVersion = "5.0.1"
val grpcVersion = "1.73.0"
val protobufVersion = "4.31.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-grpc-server:$vertxVersion")
    implementation("org.checkerframework:checker-qual:3.49.5") // needed by grpc-server somehow
    implementation("io.vertx:vertx-grpcio-server:$vertxVersion")
    implementation("io.vertx:vertx-launcher-application:$vertxVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion") // generated annotation on protos
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
}


tasks.register<JavaExec>("runClient") {
    group = "application"
    mainClass.set("com.github.louiscl.Client")
    classpath = sourceSets["main"].runtimeClasspath

    jvmArgs = listOf("-Djava.util.logging.config.file=src/main/resources/logging.properties")
}

tasks.register<JavaExec>("runServer") {
    group = "application"
    mainClass.set("com.github.louiscl.Server")
    classpath = sourceSets["main"].runtimeClasspath

    jvmArgs = listOf("-Djava.util.logging.config.file=src/main/resources/logging.properties")
}

