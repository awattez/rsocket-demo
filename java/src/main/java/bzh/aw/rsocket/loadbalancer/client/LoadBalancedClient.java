package bzh.aw.rsocket.loadbalancer.client;

import io.rsocket.core.RSocketClient;
import io.rsocket.loadbalance.LoadbalanceRSocketClient;
import io.rsocket.loadbalance.LoadbalanceTarget;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static bzh.aw.rsocket.loadbalancer.server.MultipleServers.HOST;


@Slf4j
public class LoadBalancedClient {

    static final int[] PORTS = new int[]{7000, 7001, 7002};

    public static void main(String[] args) {

        List<LoadbalanceTarget> rsocketLoadbalanceTargets = Arrays.stream(PORTS)
                .mapToObj(port -> LoadbalanceTarget.from(String.valueOf(port), TcpClientTransport.create(HOST, port))
                ).collect(Collectors.toList());

        Flux<List<LoadbalanceTarget>> producer =
                Flux.interval(Duration.ofSeconds(5))
                        .log()
                        .map(i -> i % 2 == 0 ? List.of(rsocketLoadbalanceTargets.get(0)) : rsocketLoadbalanceTargets);

        RSocketClient rSocketClient =
                LoadbalanceRSocketClient.builder(producer).roundRobinLoadbalanceStrategy().build();

        Flux.range(0, 10000)
                .flatMap(i -> rSocketClient.requestResponse(Mono.just(DefaultPayload.create("test" + i))))
                .delayElements(Duration.ofMillis(100))
                .blockLast();

    }

}
