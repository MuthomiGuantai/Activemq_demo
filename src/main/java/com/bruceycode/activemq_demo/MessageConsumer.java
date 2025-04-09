package com.bruceycode.activemq_demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {
    private final Logger logger;

    public MessageConsumer(Logger logger) {
        this.logger = logger;
    }

    @JmsListener(destination = "demo-queue")
    public void receiveMessage(String message) {
        logger.info("Received message from demo-queue: {}", message);
    }
}
