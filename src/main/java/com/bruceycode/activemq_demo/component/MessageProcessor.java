package com.bruceycode.activemq_demo.component;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("messageProcessor")
public class MessageProcessor {

    private final Logger logger;
    private int attemptCount = 0;

    @Autowired
    public MessageProcessor(Logger logger) {
        this.logger = logger;
    }

    public void processMessage(String message) {
        attemptCount++;
        logger.info("Attempt {} for message: {}", attemptCount, message);
        if (attemptCount <= 3) {
            throw new RuntimeException("Simulated failure for message: " + message);
        }
        attemptCount = 0; 
    }
}