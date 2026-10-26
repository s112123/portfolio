package org.demo.backend.global.sender;

@FunctionalInterface
public interface MessageSender {

    void send(String identifier, String data);
}
