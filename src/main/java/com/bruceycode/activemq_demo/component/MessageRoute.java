package com.bruceycode.activemq_demo.component;

import com.bruceycode.activemq_demo.entity.MessageLog;
import com.bruceycode.activemq_demo.repository.MessageLogRepository;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MessageRoute extends RouteBuilder {

    @Autowired
    private MessageLogRepository logRepository;

    @Override
    public void configure() throws Exception {
        from("direct:sendMessage")
                .routeId("sendMessageRoute")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    String enrichedBody = body + " [Sent at " + System.currentTimeMillis() + "]";
                    exchange.getIn().setBody(enrichedBody);
                    logRepository.save(new MessageLog(enrichedBody, LocalDateTime.now()));
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

        from("jms:queue:error-queue")
                .routeId("errorRoute")
                .log(LoggingLevel.WARN, "Error message received: ${body}");
    }
}
