package com.test_socket.websocket.SocketModel;

import lombok.Data;

@Data
public class MatchRequest {
    private String userId;
    private int matchId;
}

