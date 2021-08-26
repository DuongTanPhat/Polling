package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.security.CurrentUser;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.NotificationService;
@Controller
public class SocketController {
	private final NotificationService notificationService;
    @Autowired
    public SocketController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @MessageMapping("/start")
    public void start(@CurrentUser UserPrincipal currentUser,@RequestBody Long postId,StompHeaderAccessor stompHeaderAccessor) {
    	//Long postId= Long.valueOf(124);
    	System.out.println("======================================");
    	System.out.println(currentUser.getUsername());
    	System.out.println("======================================");
    	notificationService.add(currentUser.getUsername(),postId);
    }
    @MessageMapping("/stop")
    public void stop(@CurrentUser UserPrincipal currentUser,StompHeaderAccessor stompHeaderAccessor) {
    	notificationService.remove(currentUser.getUsername());
    }
}
