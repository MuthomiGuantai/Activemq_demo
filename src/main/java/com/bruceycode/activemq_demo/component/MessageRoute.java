package com.bruceycode.activemq_demo.component;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MessageRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:sendMessage")
                .routeId("sendMessageRoute")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    exchange.getIn().setBody(body + " [Sent at " + System.currentTimeMillis() + "]");
                })
                .log(LoggingLevel.INFO, "Sending message: ${body}")
                .choice()
                .when(simple("${body} contains 'error'"))
                .to("jms:queue:error-queue")
                .otherwise()
                .to("jms:queue:demo-queue")
                .end();

        from("jms:queue:demo-queue")
                .routeId("processMessageRoute")
                .errorHandler(defaultErrorHandler()
                        .maximumRedeliveries(3)
                        .redeliveryDelay(1000)
                        .logRetryStackTrace(true)
                        .retryAttemptedLogLevel(LoggingLevel.WARN))
                .bean("messageProcessor", "processMessage")
                .log(LoggingLevel.INFO, "Successfully processed: ${body}");

        from("jms:queue:dead-letter-queue")
                .routeId("dlqRoute")
                .log(LoggingLevel.ERROR, "Dead letter message: ${body}")
                .bean("messageConsumer", "receiveMessage");
    }
}
