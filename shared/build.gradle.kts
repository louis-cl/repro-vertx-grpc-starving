import com.google.protobuf.gradle.id

plugins {
    id("java-library")
    id("com.google.protobuf") version "0.9.5"
}

val grpcVersion = "1.73.0"
val protobufVersion = "4.31.1"

dependencies {
    api("com.google.protobuf:protobuf-java:${protobufVersion}")
    api("io.grpc:grpc-stub:${grpcVersion}")
    api("io.grpc:grpc-netty:${grpcVersion}")
    api("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-netty-shaded:${grpcVersion}")
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
        all().forEach {
            it.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
}
