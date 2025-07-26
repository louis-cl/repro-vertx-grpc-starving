package com.github.louiscl;

import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GrpcServer {

    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());

    Server server;
    ExecutorService executor = Executors.newFixedThreadPool(2);

    private void start() throws IOException {
        server = NettyServerBuilder.forPort(ServerConfiguration.PORT, InsecureServerCredentials.create())
                .flowControlWindow(65535)
                .initialFlowControlWindow(65535)
                .executor(executor)
                .addService(new GrpcService())
                .build()
                .start();
        logger.info("Server started, listening on " + ServerConfiguration.PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                GrpcServer.this.stop();
            } catch (InterruptedException e) {
                if (server != null) {
                    server.shutdownNow();
                }
                e.printStackTrace(System.err);
            } finally {
                executor.shutdown();
            }
            System.err.println("*** server shut down");
        }));
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        var server = new GrpcServer();
        server.start();
        server.blockUntilShutdown();
    }
}
