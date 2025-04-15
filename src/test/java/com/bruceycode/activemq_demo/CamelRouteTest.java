package com.bruceycode.activemq_demo;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest
public class CamelRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Test
    public void testMessageRoute() {
        producerTemplate.sendBody("direct:sendMessage", "TestCamel");

        await().atMost(10, SECONDS).until(() ->
                camelContext.getRoute("processMessageRoute").getUptimeMillis() > 0);
    }
}
