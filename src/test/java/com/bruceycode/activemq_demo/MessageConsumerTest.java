package com.bruceycode.activemq_demo;

import com.bruceycode.activemq_demo.component.MessageConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageConsumerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private MessageConsumer messageConsumer;

    @Test
    public void testReceiveMessage() {
        String message = "TestConsumerMessage";

        messageConsumer.receiveMessage(message);

        verify(logger, times(1)).info("Received message from demo-queue: {}", message);
    }
}
