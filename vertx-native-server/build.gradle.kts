plugins {
    id("java")
    id("application")
}

val vertxVersion = "5.0.1"

dependencies {
    implementation(project(":shared"))

    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-grpc-server:$vertxVersion")
    implementation("io.vertx:vertx-launcher-application:$vertxVersion")
}

application {
    mainClass.set("com.github.louiscl.VertxNativeServer")
}
