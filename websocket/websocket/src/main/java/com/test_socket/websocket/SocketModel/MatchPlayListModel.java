package com.test_socket.websocket.SocketModel;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("addedmatchlists")
public class MatchPlayListModel {
    private String eventId;
    private int EventTypeId;
    private String EventTypeName;
    private int GameStatus;
    private LocalDateTime MatchTime;
    private String OddsSource;
    private  String SeriesID;
    private  String FancySource;
    private  String marketId;
    private String MatchTimeIST;
    private String[] MatchTimeISTString;
    private String SeriesName;
    private String Source;
    private String WinnTeamName;
    private int WinnerTeamId;
    private LocalDateTime addDate;
    private String addDateString;
    double back1;
    double back11;
    double back12;
    //private LocalDateTime eventDate;
    //private String eventDateString;
    private String eventName;
    private int isAddedToMarket;
    private int isInPlay;
    double lay1;
    double lay11;
    double lay12;
    private int market_runner_count;
    private String runnerName1;
    private String runnerName2;
    private String runnerName3;
    private String selectionId1;
    private String selectionId2;
    private String selectionId3;
    String crexURL;
    private long OddsCount;
    private long FancyCount;

}

