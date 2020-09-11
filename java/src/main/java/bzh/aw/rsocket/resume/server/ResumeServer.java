package bzh.aw.rsocket.resume.server;

import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
public class ResumeServer {

    public static final int SERVER_PORT = 7000;
    public static final String HOST = "localhost";

    /**
     * Make the socket capable of resumption.
     * By default, the Resume Session will have a duration of 120s, a timeout of
     * 10s, and use the In Memory (volatile, non-persistent) session store.
     */
    public static Resume RESUME =
            new Resume()
                    // we can add
                    //.sessionDuration()
                    // Retry every seconds with huge value of attempts
                    .retry(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(1))
                            .doBeforeRetry(s -> log.warn("Disconnected. Trying to resume...")));


    public static void main(String[] args) throws InterruptedException {

        RSocketServer
                .create(
                        // we only implement the request stream of the interface
                        SocketAcceptor.forRequestStream(payload -> {
                            log.info("Received 'requestStream' request with payload: [{}]", payload.getDataUtf8());
                            // Every second give value of Flux interval
                            return Flux.interval(Duration.ofSeconds(1))
                                    .map(t -> DefaultPayload.create(t.toString()));
                        }))
                // RESUME capabilities on server
                .resume(RESUME)
                .bind(TcpServerTransport.create(HOST, SERVER_PORT))
                // block not subscribe
                .block();

        log.info("ResumeServer running");

        // Keep current thread UP
        Thread.currentThread().join();
    }

}
