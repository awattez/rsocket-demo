package bzh.aw.rsocket.spring.model;

import lombok.Value;

import java.util.List;

@Value
public class MultipleCustomersRequest {
    List<String> ids;
}