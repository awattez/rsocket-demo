package bzh.aw.rsocket.interaction.server;

import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;

@Slf4j
public class FullServer {

    public static final String HOST = "localhost";
    public static final int PORT = 7000;

    public static void main(String[] args) throws InterruptedException {
        RSocketServer
                .create(new HelloWorldSocketAcceptor())
                .bind(TcpServerTransport.create(HOST, PORT))
                .doOnNext(cc -> log.info("RequestResponseServer started on the address : {}", cc.address()))
                .block();

        log.info("RequestResponseServer running");

        // Keep current thread UP
        Thread.currentThread().join();
    }

    @Slf4j
    static class HelloWorldSocketAcceptor implements SocketAcceptor {

        @Override
        public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
            log.info("Received connection with setup payload: [{}] and meta-data: [{}]", setup.getDataUtf8(), setup.getMetadataUtf8());
            return Mono.just(new RSocket() {
                @Override
                public Mono<Void> fireAndForget(Payload payload) {
                    log.info("Received 'fire-and-forget' request with payload: [{}]", payload.getDataUtf8());
                    return Mono.empty();
                }

                @Override
                public Mono<Payload> requestResponse(Payload payload) {
                    log.info("Received 'request response' request with payload: [{}] ", payload.getDataUtf8());
                    return Mono.just(DefaultPayload.create("Hello " + payload.getDataUtf8()));
                }

                @Override
                public Flux<Payload> requestStream(Payload payload) {
                    log.info("Received 'request stream' request with payload: [{}] ", payload.getDataUtf8());
                    return Flux.interval(Duration.ofSeconds(1))
                            .map(time -> DefaultPayload.create("Stream -> Hello " + payload.getDataUtf8() + " @ " + Instant.now()));
                }

                @Override
                public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                    return Flux.from(payloads)
                            .doOnNext(payload -> log.info("Received payload: [{}]", payload.getDataUtf8()))
                            .map(payload -> DefaultPayload.create("Channel -> Hello " + payload.getDataUtf8() + " @ " + Instant.now()))
                            .subscribeOn(Schedulers.parallel());
                }

                @Override
                public Mono<Void> metadataPush(Payload payload) {
                    log.info("Received 'metadata push' request with metadata: [{}]", payload.getMetadataUtf8());
                    return Mono.empty();
                }
            });
        }
    }

}
