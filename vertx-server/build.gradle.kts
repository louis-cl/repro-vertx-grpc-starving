plugins {
    id("java")
    id("application")
}

val vertxVersion = "5.0.1"

dependencies {
    implementation(project(":shared"))

    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-grpc-server:$vertxVersion")
    implementation("io.vertx:vertx-grpcio-server:$vertxVersion")
    implementation("io.vertx:vertx-launcher-application:$vertxVersion")
//    implementation("org.checkerframework:checker-qual:3.49.5")
}

application {
    mainClass.set("com.github.louiscl.Server")
    applicationDefaultJvmArgs = listOf("-Djava.util.logging.config.file=src/main/resources/logging.properties")
}
