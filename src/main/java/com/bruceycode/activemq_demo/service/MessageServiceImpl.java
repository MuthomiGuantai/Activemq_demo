package com.bruceycode.activemq_demo.service;

import org.slf4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    private final Logger logger;
    private final JmsTemplate jmsTemplate;
    public int attemptCount = 0;


    public MessageServiceImpl(JmsTemplate jmsTemplate, Logger logger) {
        this.jmsTemplate = jmsTemplate;
        this.logger = logger;
    }

    @JmsListener(destination = "demo-queue")
    public void receiveMessage(String message) {
        attemptCount++;
        logger.info("Attempt {} for message: {}", attemptCount, message);
        if (attemptCount <= 3) {
            throw new RuntimeException("Simulated failure for message: " + message);
        }
        logger.info("Successfully received: {}", message);
        attemptCount = 0;
    }

    public void sendMessage(String message) {
        jmsTemplate.convertAndSend("demo-queue", message);
        logger.info("Sent message to demo-queue: {}", message);
    }
}
