package bzh.aw.rsocket.interaction.client;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import reactor.util.retry.Retry;

import java.time.Duration;

import static bzh.aw.rsocket.interaction.server.RequestResponseServer.HOST;
import static bzh.aw.rsocket.interaction.server.RequestResponseServer.PORT;


@Slf4j
public class RequestResponseClient {

    public static void main(String[] args) {
        RSocket socket = RSocketConnector
                // Simple connection to our Server
                .connectWith(TcpClientTransport.create(HOST, PORT))
                // Retry connection
                .retryWhen(Retry.backoff(50, Duration.ofMillis(500)))
                .block();

        socket.requestResponse(DefaultPayload.create("Adrien"))
                .doOnNext(payload -> log.info("Received response payload:[{}] metadata:[{}]",
                        payload.getDataUtf8(),
                        payload.getMetadataUtf8()))
                .block();

        socket.dispose();
    }

}
