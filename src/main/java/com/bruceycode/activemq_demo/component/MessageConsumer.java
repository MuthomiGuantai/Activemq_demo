package com.bruceycode.activemq_demo.component;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {
    private final Logger logger;

    public MessageConsumer(Logger logger) {
        this.logger = logger;
    }

    public void receiveMessage(String message) {
        logger.info("Received message from demo-queue: {}", message);
    }


}
