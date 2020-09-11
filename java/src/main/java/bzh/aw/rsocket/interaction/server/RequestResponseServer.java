package bzh.aw.rsocket.interaction.server;

import io.rsocket.Payload;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
public class RequestResponseServer {

    public static final String HOST = "localhost";
    public static final int PORT = 7000;

    public static void main(String[] args) throws InterruptedException {
        RSocketServer
                .create(
                        SocketAcceptor.forRequestResponse(
                                p -> {
                                    String data = p.getDataUtf8();

                                    log.info("Received request data {}", data);
                                    Payload responsePayload = DefaultPayload.create("Bonjour ! tu as demandé : " + data);

                                    // Deallocate
                                    p.release();
                                    // Simple payload
                                    return Mono.just(responsePayload);
                                }))
                .bind(TcpServerTransport.create(HOST, PORT))
                // add delay subscription we wait 10 seconds
                .delaySubscription(Duration.ofSeconds(10))
                .doOnNext(cc -> log.info("RequestResponseServer started on the address : {}", cc.address()))
                .block();

        log.info("RequestResponseServer running");

        // Keep current thread UP
        Thread.currentThread().join();
    }


}
