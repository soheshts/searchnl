package io.github.soheshts.searchnl.controller;


import io.github.soheshts.searchnl.model.ChatMessage;
import io.github.soheshts.searchnl.model.MessageType;
import io.github.soheshts.searchnl.model.Product;
import io.github.soheshts.searchnl.model.SearchCriteria;
import io.github.soheshts.searchnl.service.SearchService;
import io.github.soheshts.searchnl.tool.Tools;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Controller
public class ChatController {


    private final SimpMessageSendingOperations messagingTemplate;
    private final SearchService searchService;
    private final Tools tool;
    ObjectMapper objectMapper;

    // Constructor injection
    public ChatController(final SimpMessageSendingOperations messagingTemplate, final SearchService searchService, final Tools tool, final ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.searchService = searchService;
        this.tool = tool;
        this.objectMapper = objectMapper;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        System.out.println("----------------Received-----------------");
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
        UserMessage userMessage = new UserMessage(chatMessage.getContent());
        String response = tool.getStructuredFilter(userMessage);
        SearchCriteria searchCriteria = objectMapper.readValue(response, SearchCriteria.class);
        System.out.println("Structured response" + response);

        List<Product> products = searchService.findBySimilarity(chatMessage.getContent(),searchCriteria);


        ChatMessage botReply = ChatMessage.builder().type(MessageType.CHAT).sender("BOT").content("Here's what I found for you:").products(products).build();

        messagingTemplate.convertAndSend("/topic/public", botReply);

    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
//        chatMemory.clear("order response");
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }

}
