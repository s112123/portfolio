package org.demo.backend.global.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
//@Profile({"prod"})
//@Component
public class MailMessageSender implements MessageSender {

    @Override
    public void send(String email, String data) {
        log.info("[SEND] email={}, code={}", email, data);
    }
}
