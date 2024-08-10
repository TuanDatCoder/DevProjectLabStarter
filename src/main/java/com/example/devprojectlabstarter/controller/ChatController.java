package com.example.devprojectlabstarter.controller;


import com.example.devprojectlabstarter.dto.ChatSocket.ChatMessageDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDTO sendMessage(ChatMessageDTO chatMessageDTO) {
        return chatMessageDTO;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageDTO addUser(ChatMessageDTO chatMessageDTO) {
        chatMessageDTO.setContent(chatMessageDTO.getSender() + " joined!");
        return chatMessageDTO;
    }
    @GetMapping("/chatSocket")
    public String chatSocket() {
        return "chatSocket";
    }
}