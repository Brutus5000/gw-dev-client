package com.faforever.gw.model;

import com.faforever.gw.messaging.MessagingService;
import com.faforever.gw.messaging.incoming.BattleParticipantJoinedAssaultMessage;
import com.faforever.gw.messaging.incoming.HelloMessage;
import com.faforever.gw.messaging.outgoing.ClientMessage;
import com.faforever.gw.messaging.outgoing.InitiateAssaultMessage;
import com.faforever.gw.messaging.outgoing.JoinAssaultMessage;
import com.faforever.gw.messaging.outgoing.LeaveAssaultMessage;
import com.faforever.gw.model.entitity.Planet;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class GwClient {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessagingService messagingService;
    private final RestTemplate restTemplate;
    private String host;
    private String port;
    private Map<UUID, ClientMessage> pendingMessages = new HashMap<>();
    private ClientState clientState;
    @Getter
    private UUID currentBattle;
    @Getter
    private UUID myCharacter;

    @Inject
    public GwClient(ApplicationEventPublisher applicationEventPublisher, MessagingService messagingService, RestTemplate restTemplate) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.messagingService = messagingService;
        this.restTemplate = restTemplate;
    }

    @EventListener
    private void onHello(HelloMessage message) {
        myCharacter = message.getCharacterId();
        currentBattle = message.getCurrentBattleId();
        if (currentBattle == null) {
            clientState = ClientState.FREE_FOR_BATTLE;
        } else {
            clientState = ClientState.IN_ASSAULT;
        }
        log.debug("New client state: {}", clientState);
        applicationEventPublisher.publishEvent(clientState);
    }

    @EventListener
    private void onBattleParticipantJoined(BattleParticipantJoinedAssaultMessage message) {
        if (message.getCharacterId().equals(myCharacter)) {
            clientState = ClientState.IN_ASSAULT;
            currentBattle = message.getBattleId();
            applicationEventPublisher.publishEvent(clientState);
        }
    }

    public void connect(String host, String port, String token) {
        this.host = host;
        this.port = port;
        String uri = String.format("ws://%s:%s/websocket?accessToken=%s", host, port, token);
        messagingService.connect(uri)
                .thenAccept(session -> {
                    clientState = ClientState.CONNECTED;
                    log.debug("New client state: {}", clientState);
                    applicationEventPublisher.publishEvent(clientState);
                });
    }

    public void disconnect() {
        messagingService.disconnect();
        clientState = ClientState.DISCONNECTED;
        log.debug("New client state: {}", clientState);
        applicationEventPublisher.publishEvent(clientState);
    }

    @SneakyThrows
    public List<Planet> getPlanets() {
        URL planetListUrl = new URL(String.format("http://%s:%s/data/planet", host, port));
        ResourceConverter resourceConverter = new ResourceConverter(Planet.class);
        JSONAPIDocument<List<Planet>> planetList = resourceConverter.readDocumentCollection(planetListUrl.openStream(), Planet.class);

        return planetList.get();
    }

    public void initiateAssault(UUID planetId) throws IOException {
        messagingService.send(new InitiateAssaultMessage(planetId))
                .thenAccept(aVoid -> {
                    clientState = ClientState.IN_ASSAULT;
                    applicationEventPublisher.publishEvent(clientState);
                });
    }

    public void joinAssault(UUID battleId) throws IOException {
        messagingService.send(new JoinAssaultMessage(battleId))
                .thenAccept(aVoid -> {
                    clientState = ClientState.IN_ASSAULT;
                    applicationEventPublisher.publishEvent(clientState);
                });
    }

    public void leaveAssault() throws IOException {
        messagingService.send(new LeaveAssaultMessage(currentBattle))
                .thenAccept(aVoid -> {
                    clientState = ClientState.FREE_FOR_BATTLE;
                    applicationEventPublisher.publishEvent(clientState);
                });
    }
}
