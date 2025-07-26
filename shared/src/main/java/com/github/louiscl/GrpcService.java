package com.github.louiscl;

import com.github.louiscl.grpc.ProducerServiceGrpc;
import com.github.louiscl.grpc.Request;
import com.github.louiscl.grpc.Response;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GrpcService extends ProducerServiceGrpc.ProducerServiceImplBase {

    private static final Logger logger = Logger.getLogger(GrpcService.class.getName());

    @Override
    public StreamObserver<Request> streamingInputCall(StreamObserver<Response> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Request request) {
                responseObserver.onError(Status.FAILED_PRECONDITION.asException());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.log(Level.WARNING, "call cancelled", throwable);
            }

            @Override
            public void onCompleted() {
                logger.info("call completed");
                responseObserver.onCompleted();
            }
        };
    }
}