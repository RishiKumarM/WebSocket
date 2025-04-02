package com.test_socket.websocket.Repository;

import com.test_socket.websocket.SocketModel.Java_Odds;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JavaOddsRepository extends CrudRepository<Java_Odds, String> {

    Iterable<Java_Odds> findByEventId(@Param("eventId") String eventId);

}