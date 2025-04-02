package com.test_socket.websocket.SocketModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RedisMatchModel implements Serializable {
    String eventId;
    String marketId;
    String fancySource;

    @Override
    public String toString() {
        return "RediModel{" +
                "MatchId='" + eventId + '\'' +
                "MarketId='" + marketId + '\'' +
                "'FancySource='" + fancySource + '\'' +
                '}';
    }

}
