package bzh.aw.rsocket.interaction.client;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;

import static bzh.aw.rsocket.interaction.server.FullServer.HOST;
import static bzh.aw.rsocket.interaction.server.FullServer.PORT;

@Slf4j
public class RequestStreamClient {

    public static void main(String[] args) {

        RSocket socket = RSocketConnector
                // Simple connection to our Server
                .connectWith(TcpClientTransport.create(HOST, PORT))
                .block();


        socket.requestStream(DefaultPayload.create("Hello World !"))
                .take(10)
                .then()
                .doFinally(signalType -> socket.dispose())
                .then()
                .block();


    }

}
