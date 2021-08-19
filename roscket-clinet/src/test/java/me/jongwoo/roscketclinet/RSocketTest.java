package me.jongwoo.roscketclinet;

import me.jongwoo.roscketclinet.domain.Item;
import me.jongwoo.roscketclinet.domain.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
public class RSocketTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository repository;

    @Test
    public void verifyRemoteOperationsThroughRScoketRequestResponse() throws InterruptedException {

        // 데이터 초기화
        this.repository.deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();

        // 새 item 생성
        this.webTestClient.post().uri("/items/request-response")
                .bodyValue(new Item("Alf alarm clock", "nothing important", 19.9))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Item.class)
                .value(item -> {
                    assertThat(item.getId()).isNotNull();
                    assertThat(item.getName()).isEqualTo("Alf alarm clock");
                    assertThat(item.getDescription()).isEqualTo("nothing important");
                    assertThat(item.getPrice()).isEqualTo(19.99);
                });

        Thread.sleep(500);

        //Item 몽고디비에 저장됐는지 확인
        this.repository.findAll()
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getId()).isNotNull();
                    assertThat(item.getName()).isEqualTo("Alf alarm clock");
                    assertThat(item.getDescription()).isEqualTo("nothing important");
                    assertThat(item.getPrice()).isEqualTo(19.99);
                    return true;
                })
                .verifyComplete();
    }
}
