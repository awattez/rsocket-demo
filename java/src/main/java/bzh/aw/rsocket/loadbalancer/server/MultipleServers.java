package bzh.aw.rsocket.loadbalancer.server;

import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Slf4j
public class MultipleServers {

    public static final String HOST = "localhost";
    public static final int[] PORTS = new int[]{7000, 7001, 7002};


    public static void main(String[] args) throws InterruptedException {

        Arrays.stream(PORTS)
                .forEach(port -> RSocketServer.create(SocketAcceptor.forRequestResponse(
                                payload -> {
                                    var server = "SERVER-" + port;
                                    log.info("Received 'request response' request with payload: [{}] on server [{}]",
                                            payload.getDataUtf8(), server);
                                    return Mono.just(DefaultPayload.create("test-response-from" + server + "from request [" + payload.getDataUtf8() + "]"));
                                }))
                        .bind(TcpServerTransport.create(HOST, port))
                        .doOnNext(cc -> log.info("MultipleServer - Server started on the address : {}", cc.address()))
                        .subscribe());

        log.info("Servers running");

        // Keep current thread UP
        Thread.currentThread().join();
    }

}
