package me.jongwoo.rsocketserver.service;

import me.jongwoo.rsocketserver.repository.Item;
import me.jongwoo.rsocketserver.repository.ItemRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class RSocketService {

    private final ItemRepository repository;
    private final Sinks.Many<Item> itemsSink;


    public RSocketService(ItemRepository rePository) {
        this.repository = rePository;
        this.itemsSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @MessageMapping("newItems.request-response") // <1>
    public Mono<Item> processNewItemsViaRSocketRequestResponse(Item item) { // <2>
        return this.repository.save(item) // <3>
//                .doOnNext(savedItem -> this.itemSink.next(savedItem)); // <4>
        //  Deprecated인 FluxProcessor, EmitterProcessor의 대체 구현
				.doOnNext(savedItem -> this.itemsSink.tryEmitNext(savedItem));
    }

    @MessageMapping("newItems.request-stream")
    public Flux<Item> findItemsViaRSocketRequestStream(){
        return this.repository.findAll()
                .doOnNext(this.itemsSink::tryEmitNext);
    }

    @MessageMapping("newItems.fire-and-forget")
    public Mono<Void> processNewItemsViaRSocketFireAndForget(Item item){
        return this.repository.save(item)
                .doOnNext(savedItem -> this.itemsSink.tryEmitNext(savedItem)).then();
    }

    @MessageMapping("newItems.monitor")
    public Flux<Item> monitorNewItems(){
        return this.itemsSink.asFlux();
    }
}
