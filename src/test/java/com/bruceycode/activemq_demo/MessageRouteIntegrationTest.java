package com.bruceycode.activemq_demo;

import com.bruceycode.activemq_demo.service.MessageServiceImpl;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@CamelSpringBootTest
@ExtendWith(MockitoExtension.class)
@MockEndpoints("jms:queue:demo-queue")
public class MessageRouteIntegrationTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Mock
    private MessageServiceImpl messageProcessor;

    @BeforeEach
    public void setUp() throws Exception {

        camelContext.getRegistry().bind("fixedTimestamp", Long.valueOf(System.currentTimeMillis()));

        camelContext.getRegistry().bind("messageProcessor", messageProcessor);

        adviceWith(camelContext, "sendMessageRoute", routeBuilder -> {
            routeBuilder.weaveByToUri("jms:queue:demo-queue")
                    .replace()
                    .to("mock:demo-queue");
        });

        camelContext.getRouteController().stopRoute("processMessageRoute");
        camelContext.getRouteController().startRoute("sendMessageRoute");
    }

    @Test
    public void testDemoQueue() throws Exception {
        String message = "TestMessage";

        MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:demo-queue", MockEndpoint.class);
        mockEndpoint.expectedMessageCount(1);

        producerTemplate.sendBody("direct:sendMessage", message);

        mockEndpoint.assertIsSatisfied();
        String receivedBody = mockEndpoint.getReceivedExchanges().get(0)
                .getMessage().getBody(String.class);

        assertTrue(receivedBody.startsWith(message + " [Sent at "));
        assertTrue(receivedBody.endsWith("]"));

        String timestamp = receivedBody.substring(
                receivedBody.indexOf("[Sent at ") + 9,
                receivedBody.length() - 1
        );
        assertTrue(timestamp.matches("\\d+"));
    }
}