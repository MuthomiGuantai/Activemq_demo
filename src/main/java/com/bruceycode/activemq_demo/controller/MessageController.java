package com.bruceycode.activemq_demo.controller;

import com.bruceycode.activemq_demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public String sendMessage(@RequestParam String message) {
        messageService.sendMessage(message);
        return "Message sent: " + message;
    }
}
