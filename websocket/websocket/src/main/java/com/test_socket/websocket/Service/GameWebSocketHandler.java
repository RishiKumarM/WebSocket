package com.test_socket.websocket.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test_socket.websocket.SocketModel.Java_Odds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> lastBroadcastData = new ConcurrentHashMap<>();

    public GameWebSocketHandler(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        String gameId = null;
        String userId = null;

        if (query != null) {
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if (keyValue[0].equals("gameId")) {
                        gameId = keyValue[1];
                    } else if (keyValue[0].equals("userId")) {
                        userId = keyValue[1];
                    }
                }
            }
        }
        String existingGameId = findUserGameId(userId);

        if (existingGameId != null && !existingGameId.equals(gameId)) {
            removeUserFromGame(existingGameId, userId);
            System.out.println("User " + userId + " switched from gameId " + existingGameId + " to gameId " + gameId);
        }

        sessions.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(session);
        session.getAttributes().put("gameId", gameId);
        session.getAttributes().put("userId", userId);

        System.out.println("New WebSocket Connection: gameId=" + gameId + ", userId=" + userId);
        printActiveUserCount();
        sessions.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    private String findUserGameId(String userId) {
        for (Map.Entry<String, Set<WebSocketSession>> entry : sessions.entrySet()) {
            for (WebSocketSession session : entry.getValue()) {
                String sessionUserId = (String) session.getAttributes().get("userId");
                if (sessionUserId != null && sessionUserId.equals(userId)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private void removeUserFromGame(String gameId, String userId) {
        Set<WebSocketSession> gameSessions = sessions.get(gameId);
        if (gameSessions != null) {
            gameSessions.removeIf(session -> {
                String sessionUserId = (String) session.getAttributes().get("userId");
                return sessionUserId != null && sessionUserId.equals(userId);
            });
            if (gameSessions.isEmpty()) {
                sessions.remove(gameId);
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    public void broadcastGameData() {
        for (String gameId : getActiveMatchIds()) {
            Set<WebSocketSession> gameSessions = sessions.get(gameId);
            if (gameSessions == null || gameSessions.isEmpty()) {
                lastBroadcastData.remove(gameId);
//                System.out.println("lastBroadcastData1");
                continue;
            }

            if (lastBroadcastData.containsKey(gameId)) {
                String cachedData = lastBroadcastData.get(gameId);
                sendDataToSessions(gameSessions, cachedData);
//                System.out.println("lastBroadcastData2");
                continue;
            }

            String redisKey = "ODDS_DATA_" + gameId;
            String matchData = redisTemplate.opsForValue().get(redisKey);
            if (matchData != null) {
                try {
                    List<Java_Odds> redisData = objectMapper.readValue(matchData, new TypeReference<List<Java_Odds>>() {});
                    String jsonData = objectMapper.writeValueAsString(redisData);
                    lastBroadcastData.put(gameId, jsonData);
                    sendDataToSessions(gameSessions, jsonData);
//                    System.out.println("lastBroadcastData3");
                } catch (JsonProcessingException e) {
                    System.out.println("JSON Parsing Error: " + e.getMessage());
                }
            } else {
                System.out.println("No data found for key: " + redisKey);
                lastBroadcastData.remove(gameId);
                sendDataToSessions(gameSessions, "[]");
                sessions.remove(gameId);
            }
        }
    }

    private void sendDataToSessions(Set<WebSocketSession> gameSessions, String jsonData) {
        for (WebSocketSession session : gameSessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonData));
//                    System.out.println("lastBroadcastData4");
                } catch (IOException e) {
                    System.out.println("WebSocket Message Sending Error: " + e.getMessage());
                }
            }
        }
    }


    public Set<String> getActiveMatchIds() {
        return sessions.keySet();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> messageMap = objectMapper.readValue(payload, new TypeReference<Map<String, String>>() {});

            if ("disconnect".equals(messageMap.get("type"))) {
                String userId = messageMap.get("userId");
                if (userId != null) {
                    System.out.println("User " + userId + " is disconnecting");
                    String gameId = (String) session.getAttributes().get("gameId");
                    sessions.computeIfPresent(gameId, (k, v) -> {
                        v.remove(session);
                        return v.isEmpty() ? null : v;
                    });
                    printActiveUserCount();
                }
            }
        } catch (JsonProcessingException e) {
            System.out.println("Error processing disconnect message: " + e.getMessage());
        }
    }

    private void printActiveUserCount() {
        int totalUsers = sessions.values().stream().mapToInt(Set::size).sum();
        totalUsers = Math.max(totalUsers, 1);
        System.out.println("Active Users: " + totalUsers);
    }
}
