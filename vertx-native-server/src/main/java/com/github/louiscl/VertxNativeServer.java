package com.github.louiscl;

import com.github.louiscl.grpc.ProducerServiceGrpc;
import com.github.louiscl.grpc.Request;
import com.github.louiscl.grpc.Response;
import io.grpc.MethodDescriptor;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.GrpcMessageEncoder;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.common.ServiceMethod;
import io.vertx.grpc.common.ServiceName;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.launcher.application.VertxApplication;

import java.util.logging.Level;
import java.util.logging.Logger;

// Adapted from https://github.com/vert-x3/vertx-examples/blob/a7c89ef3dda39edec77680dfb885e62e7ac81edc/grpc-examples/src/main/java/io/vertx/example/grpc/producer/Server.java
public class VertxNativeServer extends VerticleBase {

    private static final Logger logger = Logger.getLogger(VertxNativeServer.class.getName());

    public static void main(String[] args) {
        VertxApplication.main(new String[]{VertxNativeServer.class.getName()});
    }

    @Override
    public Future<?> start() {
        // Create the server
        GrpcServer rpcServer = GrpcServer.server(vertx);
        MethodDescriptor<Request, Response> method = ProducerServiceGrpc.getStreamingInputCallMethod();
        ServiceMethod<Request, Response> serviceMethod = ServiceMethod.server(
                ServiceName.create(method.getServiceName()),
                method.getBareMethodName(),
                GrpcMessageEncoder.encoder(),
                GrpcMessageDecoder.decoder(Request.newBuilder())
        );
        rpcServer.callHandler(serviceMethod, request -> {
            request.handler(event -> {
                request.response().status(GrpcStatus.FAILED_PRECONDITION).end();
            });
            request.endHandler(event -> {
                logger.info("call completed");
                request.response().end();
            });
            request.exceptionHandler(err -> {
                logger.log(Level.WARNING, "error call", err);
            });
        });
        // start the server
        return vertx.createHttpServer().requestHandler(rpcServer).listen(ServerConfiguration.PORT);
    }
}