package wheel_casino.casino.CasinoService;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import wheel_casino.casino.CasinoModel.Model.CasinoWheelBetTable;
import wheel_casino.casino.CasinoModel.Model.CasinoWheelUserWallet;
import wheel_casino.casino.CasinoModel.Model.WheelRandomId;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private int countdown = 120;
    private String currentCasinoWheelTicketId;
    private static final String CASINO_WHEEL_ID = "CWG2025";
    private static final String CASINO_NAME = "SUPER KING CASINO"; //"CASINO WHEEL";
    private ScheduledExecutorService scheduler;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public GameWebSocketHandler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        sendGameState(session);
        System.out.println("Client connected: " + session.getId() + " -- Sessions size: " + sessions.size());
        if (sessions.size() == 1) {
            startGameCycle();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Client Removed: " + session.getId() + " -- Sessions size: " + sessions.size());
    }
    private volatile boolean gameCycleRunning = false;
    private synchronized void startGameCycle() {
        if (gameCycleRunning) return;
        gameCycleRunning = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            if (sessions.isEmpty()) return;
            long start = System.currentTimeMillis();
            if (countdown == 120) {
                currentCasinoWheelTicketId = getCasinoWheelIdFromDB();
            }
            countdown--;
            broadcastGameState();
            if (countdown <= 0) {
                countdown = 120;
            }

            long elapsed = System.currentTimeMillis() - start;
//            System.out.println("Cycle took " + elapsed + " ms");
        }, 0, 1, TimeUnit.SECONDS);
    }

    private boolean spinSent = false;
    private String result = "";
    private boolean spinNotificationSent = false;
    private void broadcastGameState() {
        Map<String, Object> data = new HashMap<>();
        data.put("casinoWheelId", CASINO_WHEEL_ID);
        data.put("casinoWheelName", CASINO_NAME);
        data.put("casinoWheelTicketId", currentCasinoWheelTicketId);
        data.put("remainingTime", formatCountdown(countdown));
        data.put("sendSpinNotification", "False");

        if (countdown <= 110 && countdown >= 1){
            data.put("result", result);
        } else {
            data.put("result", "");
        }

        if (countdown == 120 && !spinSent) {
            data.put("sendSpinNotification", "SPIN");
            spinSent = true;
//            CompletableFuture.runAsync(this::getResult);
        }

        if (countdown == 5 && !spinNotificationSent) {
            data.put("sendSpinNotification", "True");
            spinNotificationSent = true;
        }

        String json = new Gson().toJson(data);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    System.out.println("Broadcasting Error : " + e.getMessage());
                }
            }
        }
    }

    private void sendGameState(WebSocketSession session) {
        Map<String, Object> data = new HashMap<>();
        data.put("casinoWheelId", CASINO_WHEEL_ID);
        data.put("casinoWheelName", CASINO_NAME);
        data.put("casinoWheelTicketId", currentCasinoWheelTicketId);
        data.put("remainingTime", formatCountdown(countdown));
        data.put("sendSpinNotification", "False");

        if (countdown <= 110 && countdown >= 1){
            data.put("result", result);
        } else {
            data.put("result", "");
        }

        if (countdown == 120 && !spinSent) {
            data.put("sendSpinNotification", "SPIN");
            spinSent = true;
//            CompletableFuture.runAsync(this::getResult);
        }

        if (countdown == 5 && !spinNotificationSent) {
            data.put("sendSpinNotification", "True");
            spinNotificationSent = true;
        }

        String json = new Gson().toJson(data);
        try {
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            System.out.println("Broadcasting Error : " + e.getMessage());
        }
    }

    private String formatCountdown(int countdownInSeconds) {
        int minutes = countdownInSeconds / 60;
        int seconds = countdownInSeconds % 60;
        return String.format("%01d:%02d", minutes, seconds);
    }

    private LocalDateTime roundToNearestTwoMinutes(LocalDateTime now) {
        int minute = now.getMinute();
        int roundedMinute = (minute / 2) * 2;
        return now.withMinute(roundedMinute).withSecond(0).withNano(0);
    }
    public String getCasinoWheelIdFromDB() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime roundedTime = roundToNearestTwoMinutes(now);
        Query query = new Query(Criteria.where("roundStartTime").is(roundedTime));
        WheelRandomId record = mongoTemplate.findOne(query, WheelRandomId.class);
//        System.out.println("Data: " + record.getCasinoWheelTicketId());
        return record.getCasinoWheelTicketId();
    }

    @Scheduled(fixedRate = 120000)
    public void getResult() {
        Query query = new Query(Criteria.where("betStatus").is(0).and("casinoBetType").is("RCW"));
        List<CasinoWheelBetTable> records = mongoTemplate.find(query, CasinoWheelBetTable.class);
        Map<String, Double> cardTotals = new HashMap<>();
        for (CasinoWheelBetTable doc : records) {
            String selected = doc.getSelectedCardType();
            selected = selected.replaceAll("[{}]", "");
            String[] parts = selected.split(",\\s*");
            for (String part : parts) {
                String[] keyValue = part.split("=");
                String card = keyValue[0];
                double value = Double.parseDouble(keyValue[1]);
                cardTotals.put(card, cardTotals.getOrDefault(card, 0.0) + value);
            }
        }
        double minTotal = Collections.min(cardTotals.values());
        List<String> lowestCards = new ArrayList<>();
        for (Map.Entry<String, Double> entry : cardTotals.entrySet()) {
            if (entry.getValue() == minTotal) {
                lowestCards.add(entry.getKey());
            }
        }
        String selectedLowestCard = "";
        if (lowestCards.size() == 1) {
            selectedLowestCard = lowestCards.getFirst();
        } else {
            Random random = new Random();
            selectedLowestCard = lowestCards.get(random.nextInt(lowestCards.size()));
        }
        System.out.println("Selected lowest card: " + selectedLowestCard);
        result = selectedLowestCard;
    }


}
