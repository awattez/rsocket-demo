package bzh.aw.rsocket.spring.controller;

import bzh.aw.rsocket.spring.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;


@Slf4j
@Controller
public class RSocketController {

    static final String SERVER = "Server";
    static final String RESPONSE = "Response";
    static final String STREAM = "Stream";
    static final String CHANNEL = "Channel";


    /**
     * This @MessageMapping is intended to be used "request --> response" style.
     * For each Message received, a new Message is returned with ORIGIN=Server and INTERACTION=Request-Response.
     *
     * @param request message
     * @return Message
     */
    @MessageMapping("request-response")
    Mono<Message> requestResponse(final Message request) {
        log.info("Received request-response request: {}", request);
        // create a single Message and return it
        return Mono.just(new Message(SERVER, RESPONSE));
    }

    /**
     * This @MessageMapping is intended to be used "fire --> forget" style.
     * When a new CommandRequest is received, nothing is returned (void)
     *
     * @param request message
     * @return Void
     */
    @MessageMapping("fire-and-forget")
    public Mono<Void> fireAndForget(final Message request) {
        log.info("Received fire-and-forget request: {}", request);
        return Mono.empty();
    }

    /**
     * This @MessageMapping is intended to be used "subscribe --> stream" style.
     * When a new request command is received, a new stream of events is started and returned to the client.
     *
     * @param request message
     * @return flux of message
     */
    @MessageMapping("stream")
    Flux<Message> stream(final Message request) {
        log.info("Received stream request: {}", request);
        return Flux
                // create a new indexed Flux emitting one element every second
                .interval(Duration.ofSeconds(1))
                // create a Flux of new Messages using the indexed Flux
                .map(index -> new Message(SERVER, STREAM, index));
    }

    /**
     * This @MessageMapping is intended to be used "stream <--> stream" style. aka Channel
     *
     * @param messages multiple or single message
     * @return multiple message for each stream or event from messages parameter
     */
    @MessageMapping("channel")
    Flux<Message> channel(final Flux<Message> messages) {
        log.info("Received channel request...");

        return messages
                .doOnNext(message -> log.info("Channel message is {}", message.getMessage()))
                .doOnCancel(() -> log.warn("The client cancelled the channel."))
                .switchMap(message -> Flux.interval(Duration.ofSeconds(1))
                        .map(index -> new Message(SERVER, CHANNEL, index)));
    }
}
