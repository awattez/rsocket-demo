package bzh.aw.rsocket.interaction.client;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static bzh.aw.rsocket.interaction.server.FullServer.HOST;
import static bzh.aw.rsocket.interaction.server.FullServer.PORT;

public class ChannelClient {

    public static void main(String[] args) {
        RSocket socket = RSocketConnector
                // Simple connection to our Server
                .connectWith(TcpClientTransport.create(HOST, PORT))
                .block();


        socket.requestChannel(Flux.interval(Duration.ofMillis(1000))
                .map(time -> DefaultPayload.create("JUG SUMMERCAMP & SNOWCAMP")))
                .doFinally(signalType -> socket.dispose())
                .blockLast();
    }

}
