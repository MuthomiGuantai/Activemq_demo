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
                .log(LoggingLevel.INFO, "Sending message: ${body}")
                .to("jms:queue:demo-queue");

        from("jms:queue:demo-queue")
                .routeId("processMessageRoute")
                .errorHandler(defaultErrorHandler()
                        .maximumRedeliveries(3)
                        .redeliveryDelay(1000)
                        .logRetryStackTrace(true)
                        .retryAttemptedLogLevel(LoggingLevel.WARN))
                .bean("messageProcessor", "processMessage")
                .log(LoggingLevel.INFO, "Successfully processed: ${body}");
    }
}
