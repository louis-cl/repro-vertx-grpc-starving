package com.github.louiscl;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.grpcio.server.GrpcIoServer;
import io.vertx.grpcio.server.GrpcIoServiceBridge;
import io.vertx.launcher.application.VertxApplication;

// Adapted from https://github.com/vert-x3/vertx-examples/blob/a7c89ef3dda39edec77680dfb885e62e7ac81edc/grpc-examples/src/main/java/io/vertx/example/grpc/producer/Server.java
public class Server extends VerticleBase {

    public static void main(String[] args) {
        VertxApplication.main(new String[]{Server.class.getName()});
    }

    @Override
    public Future<?> start() {
        // Create the server
        GrpcIoServer rpcServer = GrpcIoServer.server(vertx);
        rpcServer.addService(GrpcIoServiceBridge.bridge(new GrpcService()));

        // start the server
        return vertx.createHttpServer().requestHandler(rpcServer).listen(8080);
    }
}