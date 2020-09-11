package bzh.aw.rsocket.resume.client;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;

import static bzh.aw.rsocket.resume.server.ResumeServer.HOST;
import static bzh.aw.rsocket.resume.server.ResumeServer.RESUME;

@Slf4j
public class ResumeClient {

    private static final int CLIENT_PORT = 7001;

    public static void main(String[] args) {

        RSocket socket = RSocketConnector
                .create()
                // Create connection with RESUME object to configure resume feature
                .resume(RESUME)
                .connect(TcpClientTransport.create(HOST, CLIENT_PORT))
                .block();

        // Simple Request Stream
        socket.requestStream(DefaultPayload.create("dummy"))
                // for each stream
                .doOnNext(payload -> log.info("Received data: [{}]", payload.getDataUtf8()))
                .blockLast();

    }
}
