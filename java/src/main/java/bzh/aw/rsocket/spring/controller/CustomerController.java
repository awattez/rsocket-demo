package bzh.aw.rsocket.spring.controller;

import bzh.aw.rsocket.spring.model.CustomerRequest;
import bzh.aw.rsocket.spring.model.CustomerResponse;
import bzh.aw.rsocket.spring.model.MultipleCustomersRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Controller
@Slf4j
public class CustomerController {

    private final List<String> RANDOM_NAMES = Arrays.asList("Adrien", "Andrew", "Joe", "Matt", "Rachel", "Robin", "Jack");

    @MessageMapping("customer")
    CustomerResponse getCustomer(CustomerRequest customerRequest) {
        return new CustomerResponse(customerRequest.getId(), getRandomName());
    }

    @MessageMapping("customer-stream")
    Flux<CustomerResponse> getCustomers(MultipleCustomersRequest multipleCustomersRequest) {
        return Flux.range(0, multipleCustomersRequest.getIds().size())
                .delayElements(Duration.ofMillis(500))
                .map(i -> new CustomerResponse(multipleCustomersRequest.getIds().get(i), getRandomName()));
    }

    @MessageMapping("customer-channel")
    Flux<CustomerResponse> getCustomersChannel(Flux<CustomerRequest> requests) {
        return Flux.from(requests)
                .doOnNext(message -> log.info("Received 'customerChannel' request [{}]", message))
                .map(message -> new CustomerResponse(message.getId(), getRandomName()));
    }

    private String getRandomName() {
        return RANDOM_NAMES.get(new Random().nextInt(RANDOM_NAMES.size() - 1));
    }
}
