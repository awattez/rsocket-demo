package bzh.aw.rsocket.backpressure;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.time.Duration;

import static bzh.aw.rsocket.interaction.server.FullServer.HOST;
import static bzh.aw.rsocket.interaction.server.FullServer.PORT;

@Slf4j
public class RequestStreamBackPressureWithMetada {

    public static void main(String[] args) throws InterruptedException {

        RSocket socket = RSocketConnector
                // Simple connection to our Server
                .connectWith(TcpClientTransport.create(HOST, PORT))
                .block();

        // Here we can add some metadata
        socket.requestStream(DefaultPayload.create("Shazam", "example-metadata"))
                // rate limit request
                //.take(10)
                // Take Stream during 1 minutes
                .take(Duration.ofSeconds(60))
                // Subscribe to stream with a subscriber 'BackPressureSubscriber'
                .subscribe(new BackPressureSubscriber());

        // Keep current thread UP
        Thread.currentThread().join();
    }

    @Slf4j
    private static class BackPressureSubscriber implements Subscriber<Payload> {

        private static final Integer NUMBER_OF_REQUESTS_TO_PROCESS = 5;
        int receivedItems;
        private Subscription subscription;

        @Override
        public void onSubscribe(Subscription s) {
            this.subscription = s;
            subscription.request(NUMBER_OF_REQUESTS_TO_PROCESS);
        }

        @Override
        public void onNext(Payload payload) {
            receivedItems++;
            if (receivedItems % NUMBER_OF_REQUESTS_TO_PROCESS == 0) {
                log.info("Requesting next [{}] elements", receivedItems);
                subscription.request(NUMBER_OF_REQUESTS_TO_PROCESS);
            }
        }

        @Override
        public void onError(Throwable t) {
            log.error("Stream subscription error [{}]", t.getMessage(), t);
        }

        @Override
        public void onComplete() {
            log.info("Completing subscription");
        }
    }

}
