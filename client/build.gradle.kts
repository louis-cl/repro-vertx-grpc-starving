plugins {
    id("java")
    id("application")
}

dependencies {
    implementation(project(":shared"))
}

application {
    mainClass.set("com.github.louiscl.Client")
    applicationDefaultJvmArgs = listOf("-Djava.util.logging.config.file=src/main/resources/logging.properties")
}