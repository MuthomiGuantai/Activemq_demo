package com.bruceycode.activemq_demo.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class JmsConfiguration {

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        factory.setUserName("admin");
        factory.setPassword("admin");
        return factory;
    }

    @Bean
    public JmsComponent jmsComponent(ActiveMQConnectionFactory connectionFactory) {
        JmsComponent jms = new JmsComponent();
        jms.setConnectionFactory(connectionFactory);
        return jms;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ActiveMQConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setBackOff(new FixedBackOff(1000L, 3L));
        factory.setErrorHandler(t -> System.err.println("Error in listener: " + t.getMessage()));
        factory.setSessionTransacted(true);
        return factory;
    }

}
