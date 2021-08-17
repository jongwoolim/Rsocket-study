package me.jongwoo.rsocketserver.service;

import me.jongwoo.rsocketserver.repository.ItemRepository;
import org.springframework.stereotype.Service;

@Service
public class RSocketService {

    private final ItemRepository rePository;

    public RSocketService(ItemRepository rePository) {
        this.rePository = rePository;
    }
}
