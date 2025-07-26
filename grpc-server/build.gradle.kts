plugins {
    id("java")
    id("application")
}

dependencies {
    implementation(project(":shared"))
}

application {
    mainClass.set("com.github.louiscl.GrpcServer")
}