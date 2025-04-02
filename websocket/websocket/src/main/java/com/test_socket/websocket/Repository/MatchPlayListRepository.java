package com.test_socket.websocket.Repository;

import com.test_socket.websocket.SocketModel.MatchPlayListModel;
import com.test_socket.websocket.SocketModel.RedisMatchModel;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchPlayListRepository extends MongoRepository<MatchPlayListModel, String> {

    void deleteByEventId(String eventId);

    @Query("{'eventId':?0}")
    MatchPlayListModel findByEventId(String eventId);

    List<MatchPlayListModel> findByAddDateIsBefore(LocalDateTime preThreeDays);

    @Query(value ="{ 'GameStatus' : ?0, 'isAddedToMarket' : ?1 }",
            fields = "{ 'eventId': 1}")
    List<RedisMatchModel> findByGameStatusAndIsAddedToMarket(int GameStatus, int isAddedToMarket);

    @Query(value ="{ 'GameStatus' : ?0, 'isAddedToMarket' : ?1 , 'EventTypeId' : ?2}",
            fields = "{ 'eventId': 1}")
    List<RedisMatchModel> findByGameStatusAndIsAddedToMarketAndEventTypeId(int GameStatus, int isAddedToMarket, int EventTypeId);


//    @Query("{ 'GameStatus' : ?0, 'isAddedToMarket' : ?1,'EventTypeId':?2 }")
//    List<MatchPlayListModel> findByGameStatusAndIsAddedToMarketAndEventTypeId(int GameStatus, int isAddedToMarket, int EventTypeId);

}
