package com.github.louiscl;

import com.github.louiscl.grpc.ProducerServiceGrpc;
import com.github.louiscl.grpc.Request;
import com.github.louiscl.grpc.Response;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public static void main(String[] args) throws InterruptedException {
        var channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        var stub = ProducerServiceGrpc.newStub(channel);
        for (int i = 0; i < 3; i++) {
            try {
                streaming(stub, 60);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Error during call", e);
            }
        }
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

        var req = Request.newBuilder().setName("msg-1234".repeat(100)).build();
        logger.info("Each message is " + req.getSerializedSize() + " bytes");  // 1000 bytes
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
