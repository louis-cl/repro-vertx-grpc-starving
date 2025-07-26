package com.github.louiscl;

import com.github.louiscl.grpc.ProducerServiceGrpc;
import com.github.louiscl.grpc.Request;
import com.github.louiscl.grpc.Response;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.grpcio.server.GrpcIoServer;
import io.vertx.grpcio.server.GrpcIoServiceBridge;
import io.vertx.launcher.application.VertxApplication;

import java.util.logging.Level;
import java.util.logging.Logger;

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
        return vertx
                .createHttpServer()
                .requestHandler(rpcServer)
                .listen(8080);
    }

    // Inspired by https://github.com/grpc/grpc-java/blob/42e1829b3724c0fb20910c0abe70099994856307/examples/src/main/java/io/grpc/examples/routeguide/RouteGuideServer.java#L169-L213
    public class GrpcService extends ProducerServiceGrpc.ProducerServiceImplBase {

        private static final Logger logger = Logger.getLogger(GrpcService.class.getName());

        @Override
        public StreamObserver<Request> streamingInputCall(StreamObserver<Response> responseObserver) {
            return new StreamObserver<>() {
                int requestCount = 0;

                @Override
                public void onNext(Request request) {
                    requestCount++;
                    if (requestCount >= 3) {
                        responseObserver.onError(Status.FAILED_PRECONDITION.asException());
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.log(Level.WARNING, "call cancelled", throwable);
                }

                @Override
                public void onCompleted() {
                    responseObserver.onNext(Response.newBuilder().setMessage(String.valueOf(requestCount)).build());
                    responseObserver.onCompleted();
                    logger.info("call completed");
                }
            };
        }
    }
}