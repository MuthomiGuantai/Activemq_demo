package com.bruceycode.activemq_demo;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@SpringBootApplication
@EnableJms
public class ActivemqDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ActivemqDemoApplication.class, args);
	}

	@Bean
	public Logger logger() {
		return LoggerFactory.getLogger("com.bruceycode.activemq_demo");
	}

}
