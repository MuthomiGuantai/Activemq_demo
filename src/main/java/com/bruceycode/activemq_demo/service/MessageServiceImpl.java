package com.bruceycode.activemq_demo.service;

import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    private final Logger logger;
    private final ProducerTemplate producerTemplate;
    public int attemptCount = 0;


    public MessageServiceImpl( Logger logger, ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
        this.logger = logger;
    }

    public void processMessage(String message) {
        attemptCount++;
        logger.info("Attempt {} for message: {}", attemptCount, message);
        if (attemptCount <= 3) {
            throw new RuntimeException("Simulated failure for message: " + message);
        }
        logger.info("Successfully received: {}", message);
        attemptCount = 0;
    }

    public void sendMessage(String message) {
        producerTemplate.sendBody("direct:sendMessage", message);
        logger.info("Sent message to demo-queue: {}", message);
    }
}
