package com.test_socket.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class WebsocketApplicationTests {
	public static void main(String[] args) {
		SpringApplication.run(WebsocketApplication.class, args);
		System.out.println("✅ WebSocket Server Started...");
	}
}