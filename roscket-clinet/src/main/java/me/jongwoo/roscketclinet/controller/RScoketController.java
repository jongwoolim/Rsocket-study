package me.jongwoo.roscketclinet.controller;

import me.jongwoo.roscketclinet.domain.Item;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_ROUTING;
import static org.springframework.http.MediaType.*;

@RestController
public class RScoketController {

    private final Mono<RSocketRequester> requester;

    public RScoketController(RSocketRequester.Builder builder) {
        this.requester = builder
            .dataMimeType(APPLICATION_JSON)
            .metadataMimeType(parseMediaType(MESSAGE_RSOCKET_ROUTING.toString()))
                .connectTcp("localhost", 7000)
                .retry(5)
                .cache();
    }

    @PostMapping("/items/request-response")
    public Mono<ResponseEntity<?>> addNewItemUsingRSocketRequestResponse(@RequestBody Item item){
        return this.requester
                .flatMap(rSocketRequester -> rSocketRequester
                    .route("newItems.request-response")
                    .data(item)
                    .retrieveMono(Item.class))
                .map(savedItem -> ResponseEntity.created(
                        URI.create("/items/request-response")).body(savedItem));
    }

    @GetMapping(value = "/items/request-stream", produces = APPLICATION_NDJSON_VALUE)
    public Flux<Item> findItemsUsingRScoketRequestStream(){
        return this.requester
                .flatMapMany(rSocketRequester -> rSocketRequester
                    .route("/newItems.request-stream")
                    .retrieveFlux(Item.class)
                    .delayElements(Duration.ofSeconds(1)));
    }

    @PostMapping("/items/fire-and-forget")
    public Mono<ResponseEntity<?>> addNewItemUsingRSocketFireAndForget(@RequestBody Item item){
        return this.requester
                .flatMap(rSocketRequester -> rSocketRequester
                    .route("newItems.fire-and-forget")
                    .data(item)
                    .send())
                .then(
                        Mono.just(ResponseEntity.created(URI.create("/items/fire-and-forget")).build()));
    }
}
