package com.github.louiscl;

import com.github.louiscl.grpc.ProducerServiceGrpc;
import com.github.louiscl.grpc.Request;
import com.github.louiscl.grpc.Response;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public static void main(String[] args) throws InterruptedException {
        int n = Integer.parseInt(args[0]);
        logger.log(Level.INFO, "Client will send " + n + " messages per call");
        var channel = ManagedChannelBuilder.forAddress("localhost", ServerConfiguration.PORT)
                .usePlaintext()
                .build();

        var stub = ProducerServiceGrpc.newStub(channel);
        var start = Instant.now();
        for (int i = 0; i < 3; i++) {
            try {
                streaming(stub, n);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Error during call", e);
            }
        }
        logger.info("Calls took " + Duration.between(start, Instant.now()).toMillis() + " ms");
        logger.info("Shutting down");
        channel.shutdownNow();
    }

    private static void streaming(ProducerServiceGrpc.ProducerServiceStub stub, int messages) throws InterruptedException {
        // inspired by https://github.com/grpc/grpc-java/blob/42e1829b3724c0fb20910c0abe70099994856307/examples/src/main/java/io/grpc/examples/routeguide/RouteGuideClient.java#L125-L184
        CountDownLatch finishLatch = new CountDownLatch(1);
        var requestObserver = stub.streamingInputCall(new StreamObserver<>() {
            @Override
            public void onNext(Response response) {
                logger.info("Received response: " + response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "Error receiving response", t);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        });

        var req = Request.newBuilder().setName("1234567890").build();
        logger.info("Each message is " + req.getSerializedSize() + " bytes");  // 12 bytes
        try {
            for (int i = 0; i < messages; i++) {
                requestObserver.onNext(req);
                if (finishLatch.getCount() == 0) {
                    return;
                }
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e); // cancel rpc
            throw e;
        }
        requestObserver.onCompleted();
        if (!finishLatch.await(10, TimeUnit.SECONDS)) {
            logger.warning("Timing out !");
        }
    }
}
