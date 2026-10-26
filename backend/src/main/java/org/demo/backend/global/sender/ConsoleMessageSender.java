package org.demo.backend.global.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsoleMessageSender implements MessageSender {

    @Override
    public void send(String identifier, String data) {
        log.info("[SEND] identifier={}, code={}", identifier, data);
    }
}
