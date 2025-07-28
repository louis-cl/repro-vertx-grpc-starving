# TLDR;
Run
```shell
./gradlew :vertx-server:run
```
and
```shell
./gradlew :client:run
```
Client takes >20s to complete.

# Commands
Start grpc server
```shell
./gradlew :grpc-server:run
```

Start vertx server
```shell
./gradlew :vertx-server:run
```

Start client
```shell
./gradlew :client:run
```


# Scenario
An http server with a http2 connection/stream window size of 65535.

A [grpc server streaming method](shared/src/main/java/com/github/louiscl/GrpcService.java) that calls `onError` at first message.

A [client](client/src/main/java/com/github/louiscl/Client.java) that will do 3 calls sending N grpc messages of 1000 bytes (actual http size is higher).

Client waits on error or completion of the grpc call for 10s before going to the next call.
If call is completed before all messages are sent, client does an early return.

Since server fails instantly, we expect client to complete relatively quickly.
Http2 frames can be obtained from Http2FrameLogger output or via Wireshark.
Client will log the time to complete all rpc calls ignoring channel, stub setup and cleanup.

## Simple scenario
N=10, everything fits in a window and client can end before server
```shell
./gradlew :client:run --args="10"
```
### Grpc server

Timeline is:
1. Http2 settings exchange
2. Client sends request headers, data and half closes
3. Server sends response headers FAILED_PRECONDITION and half closes
4. Client observer notices FAILED_PRECONDITION and latches

A new request starts and repeats the process from step 2.
All 3 calls take 201ms

### Vertx server
Vertx server behaves the same minus some PING frames.

## Filling window scenario
N=6000, with at least 12 bytes per message, this doesn't fit in the default http2 windows
```shell
./gradlew :client:run --args="6000"
```
### Grpc server

1. Http2 settings exchange
2. Client sends request headers, multiple data frames
3. Server sends response headers FAILED_PRECONDITION and half closes
4. Client observer notices FAILED_PRECONDITION and latches, next request can start.
5. Server sends window updates for the connection and updates it's settings to have a bigger initial window.
6. Client sends data for the first request and half closes. First stream is closed.
7. New request starts.

All the 3 calls take 265ms on my machine.

### Vertx server

1. Http2 settings exchange
2. Client sends request headers, multiple data frames
3. Server sends response headers FAILED_PRECONDITION and half closes
4. Client observer notices FAILED_PRECONDITION and latches.
5. 2nd request starts with headers, but previous one isn't done.
6. 2nd timeout after 10s and latches
7. 3rd request starts
8. 3rd request timeout after 10s and channel shutdowns
9. Client observer notices UNAVAILABLE (Channel shutdownNow invoked)
10. Client sends RST_STREAM frames for 2nd and 3rd streams with cancel (error=8)

All the 3 calls take 20s on my machine.

Notable differences with the grpc server:
- Instantly starts new request while grpc seems to wait until first one is closed
- No data frames are sent after the first call
- No window update frames received from server after half close with headers
- No pings
- Grpc adjusts it's initial window size after the first request (likely because by default it has a bigger window)

## Interpretation
Client exhausts the connection window with the first call.
Then other streams start but can't send data frames because the window is full.
Server doesn't receive any messages so it doesn't fail, leading to timeout.
Somehow server doesn't send window update frames for the connection.