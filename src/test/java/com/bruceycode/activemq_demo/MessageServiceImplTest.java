package com.bruceycode.activemq_demo;

import com.bruceycode.activemq_demo.service.MessageServiceImpl;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTest {

    @Mock
    private ProducerTemplate producerTemplate;

    @Mock
    private Logger logger;

    @InjectMocks
    private MessageServiceImpl messageService;


    @Test
    public void testSendMessage() {
        String message = "TestMessage";
        String destination = "demo-queue";

        messageService.sendMessage(message);

        verify(producerTemplate, times(1)).sendBody("direct:sendMessage", message);
        verify(logger, times(1)).info("Sent message to demo-queue: {}", message);
    }

    @Test
    public void testReceiveMessageFailsThreeTimesThenSucceeds() {
        String message = "RetryMessage";

        for (int i = 1; i <= 3; i++) {
            try {
                messageService.processMessage(message);
            } catch (RuntimeException e) {
            }
            verify(logger, times(1)).info("Attempt {} for message: {}", i, message);
        }

        messageService.processMessage(message);
        verify(logger, times(1)).info("Successfully received: {}", message);

        verify(logger, times(4)).info(anyString(), any(), any());
        verifyNoMoreInteractions(logger);
    }
}
