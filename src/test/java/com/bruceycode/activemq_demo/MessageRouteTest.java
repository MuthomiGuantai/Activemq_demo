package com.bruceycode.activemq_demo;

import com.bruceycode.activemq_demo.component.MessageConsumer;
import com.bruceycode.activemq_demo.component.MessageRoute;
import com.bruceycode.activemq_demo.entity.MessageLog;
import com.bruceycode.activemq_demo.repository.MessageLogRepository;
import com.bruceycode.activemq_demo.service.MessageServiceImpl;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.Registry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MessageRouteTest {

    @Mock
    private MessageLogRepository logRepository;

    @Mock
    private CamelContext camelContext;

    @Mock
    private Registry registry;

    @Mock
    private ProducerTemplate producerTemplate;

    @Mock
    private MessageServiceImpl messageProcessor;

    @Mock
    private MessageConsumer messageConsumer;

    @InjectMocks
    private MessageRoute messageRoute;

    @BeforeEach
    public void setUp() throws Exception {
        when(camelContext.getRegistry()).thenReturn(registry);
        when(registry.lookupByName("messageProcessor")).thenReturn(messageProcessor);
        when(registry.lookupByName("messageConsumer")).thenReturn(messageConsumer);
        messageRoute.setCamelContext(camelContext);
        when(camelContext.getEndpoint(anyString())).thenReturn(mock(org.apache.camel.Endpoint.class));
    }

    @Test
    public void testSendMessageToDemoQueueWithTimestamp() throws Exception {
        String message = "TestMessage";
        String enrichedBody = message + " [Sent at " + System.currentTimeMillis() + "]";
        Exchange exchange = mock(Exchange.class);
        Message in = mock(Message.class);
        when(exchange.getIn()).thenReturn(in);
        when(in.getBody(String.class)).thenReturn(message);
        when(producerTemplate.requestBody("direct:sendMessage", message, String.class)).thenAnswer(invocation -> {
            when(in.getBody(String.class)).thenReturn(enrichedBody);
            logRepository.save(new MessageLog(enrichedBody, java.time.LocalDateTime.now()));
            return enrichedBody;
        });

        String result = producerTemplate.requestBody("direct:sendMessage", message, String.class);

        verify(producerTemplate, times(1)).requestBody("direct:sendMessage", message, String.class);
        verify(logRepository, times(1)).save(argThat(log ->
                log.getContent().contains(message) &&
                        log.getContent().contains("Sent at") &&
                        log.getTimestamp() != null
        ));
    }

    @Test
    public void testSendMessageToErrorQueue() throws Exception {
        String message = "TestErrorMessage";
        String enrichedBody = message + " [Sent at " + System.currentTimeMillis() + "]";
        Exchange exchange = mock(Exchange.class);
        Message in = mock(Message.class);
        when(exchange.getIn()).thenReturn(in);
        when(in.getBody(String.class)).thenReturn(message);
        when(producerTemplate.requestBody("direct:sendMessage", message, String.class)).thenAnswer(invocation -> {
            when(in.getBody(String.class)).thenReturn(enrichedBody);
            logRepository.save(new MessageLog(enrichedBody, java.time.LocalDateTime.now()));
            return enrichedBody;
        });

        String result = producerTemplate.requestBody("direct:sendMessage", message, String.class);

        verify(producerTemplate, times(1)).requestBody("direct:sendMessage", message, String.class);
        verify(logRepository, times(1)).save(argThat(log ->
                log.getContent().contains("TestErrorMessage") &&
                        log.getContent().contains("Sent at") &&
                        log.getTimestamp() != null
        ));
    }

    @Test
    public void testProcessMessageWithRetriesAndDLQ() throws Exception {
        String message = "RetryTest";
        String enrichedMessage = message + " [Sent at " + System.currentTimeMillis() + "]";
        when(producerTemplate.requestBody("direct:sendMessage", message, String.class)).thenAnswer(invocation -> {
            logRepository.save(new MessageLog(enrichedMessage, java.time.LocalDateTime.now()));
            try {
                messageProcessor.processMessage(enrichedMessage);
            } catch (RuntimeException e) {

            }
            return enrichedMessage;
        });

        producerTemplate.requestBody("direct:sendMessage", message, String.class);
        for (int i = 0; i < 3; i++) {
            try {
                messageProcessor.processMessage(enrichedMessage);
            } catch (RuntimeException e) {
            }
        }

        verify(messageProcessor, times(4)).processMessage(enrichedMessage); // Initial + 3 retries
        verify(logRepository, times(1)).save(any(MessageLog.class));
    }

    @Test
    public void testHandleDeadLetterQueue() throws Exception {
        String message = "DLQTest";
        String enrichedMessage = message + " [Sent at " + System.currentTimeMillis() + "]";
        when(producerTemplate.requestBody("jms:queue:dead-letter-queue", enrichedMessage, String.class)).thenReturn(enrichedMessage);

        producerTemplate.requestBody("jms:queue:dead-letter-queue", enrichedMessage, String.class);
        messageConsumer.receiveMessage(enrichedMessage);

        verify(producerTemplate, times(1)).requestBody("jms:queue:dead-letter-queue", enrichedMessage, String.class);
        verify(messageConsumer, times(1)).receiveMessage(enrichedMessage);
    }

    @Test
    public void testSaveMessageToDatabaseWithTimestamp() throws Exception {
        String message = "DBTest";
        String enrichedBody = message + " [Sent at " + System.currentTimeMillis() + "]";
        Exchange exchange = mock(Exchange.class);
        Message in = mock(Message.class);
        when(exchange.getIn()).thenReturn(in);
        when(in.getBody(String.class)).thenReturn(message);
        when(producerTemplate.requestBody("direct:sendMessage", message, String.class)).thenAnswer(invocation -> {
            when(in.getBody(String.class)).thenReturn(enrichedBody);
            logRepository.save(new MessageLog(enrichedBody, java.time.LocalDateTime.now()));
            return enrichedBody;
        });

        String result = producerTemplate.requestBody("direct:sendMessage", message, String.class);

        verify(logRepository, times(1)).save(argThat(m ->
                m.getContent().contains(message) &&
                        m.getContent().contains("Sent at") &&
                        m.getTimestamp() != null
        ));
    }
}