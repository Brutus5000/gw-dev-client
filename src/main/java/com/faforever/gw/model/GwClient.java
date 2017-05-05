package com.faforever.gw.model;

import com.faforever.gw.messaging.MessagingService;
import com.faforever.gw.messaging.incoming.BattleParticipantJoinedAssaultMessage;
import com.faforever.gw.messaging.incoming.BattleUpdateWaitingProgressMessage;
import com.faforever.gw.messaging.incoming.HelloMessage;
import com.faforever.gw.messaging.incoming.PlanetConqueredMessage;
import com.faforever.gw.messaging.incoming.PlanetDefendedMessage;
import com.faforever.gw.messaging.incoming.PlanetUnderAssaultMessage;
import com.faforever.gw.messaging.outgoing.ClientMessage;
import com.faforever.gw.messaging.outgoing.InitiateAssaultMessage;
import com.faforever.gw.messaging.outgoing.JoinAssaultMessage;
import com.faforever.gw.messaging.outgoing.LeaveAssaultMessage;
import com.faforever.gw.model.entitity.Battle;
import com.faforever.gw.model.entitity.Planet;
import com.faforever.gw.model.event.BattleUpdateWaitingProgressEvent;
import com.faforever.gw.model.event.NewBattleEvent;
import com.faforever.gw.model.event.PlanetDefendedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

@Slf4j
@Service
public class GwClient {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessagingService messagingService;
    private final ObjectMapper jsonObjectMapper;
    private final SimpleClientHttpRequestFactory simpleClientHttpRequestFactory;
    private String host;
    private String port;
    private Map<UUID, ClientMessage> pendingMessages = new HashMap<>();
    private ClientState clientState;
    @Getter
    private UUID currentBattle;
    @Getter
    private UUID myCharacter;

    @Inject
    public GwClient(ApplicationEventPublisher applicationEventPublisher, MessagingService messagingService, ObjectMapper jsonObjectMapper) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.messagingService = messagingService;
        this.jsonObjectMapper = jsonObjectMapper;
        this.simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
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

    @EventListener
    private void onPlanetConquered(PlanetConqueredMessage message) {
        if (message.getBattleId().equals(currentBattle)) {
            clientState = ClientState.FREE_FOR_BATTLE;
            log.debug("Current battle - planet conquered -> New client state: {}", clientState);
            applicationEventPublisher.publishEvent(clientState);
        }

        applicationEventPublisher.publishEvent(new PlanetDefendedEvent(getBattle(message.getBattleId())));
    }

    @EventListener
    private void onPlanetDefended(PlanetDefendedMessage message) {
        if (message.getBattleId().equals(currentBattle)) {
            clientState = ClientState.FREE_FOR_BATTLE;
            log.debug("Current battle - planet defended -> New client state: {}", clientState);
            applicationEventPublisher.publishEvent(clientState);
        }

        applicationEventPublisher.publishEvent(new PlanetDefendedEvent(getBattle(message.getBattleId())));
    }

    public void connect(String host, String port, String token) {
        this.host = host;
        this.port = port;
        String uri = String.format("ws://%s:%s/websocket?accessToken=%s", host, port, token);
        messagingService.connect(uri);
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

    @SneakyThrows
    public List<Battle> getInitiatedBattles() {
        URL battleListUrl = new URL(String.format("http://%s:%s/data/battle?filter[battle]=status==INITIATED", host, port));
        ResourceConverter resourceConverter = new ResourceConverter(Battle.class);
        JSONAPIDocument<List<Battle>> battleList = resourceConverter.readDocumentCollection(battleListUrl.openStream(), Battle.class);

        return battleList.get();
    }

    public void initiateAssault(UUID planetId) throws IOException {
        messagingService.send(new InitiateAssaultMessage(planetId))
                .thenAccept(aVoid -> {
                    clientState = ClientState.IN_ASSAULT;
                    applicationEventPublisher.publishEvent(clientState);
                });
    }

    public void joinAssault(String battleId) throws IOException {
        joinAssault(UUID.fromString(battleId));
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
                    currentBattle = null;
                    applicationEventPublisher.publishEvent(clientState);
                });
    }

    @SneakyThrows
    private Battle getBattle(UUID battleId) {
        URL battleUrl = new URL(String.format("http://%s:%s/data/battle/%s", host, port, battleId.toString()));
        ResourceConverter resourceConverter = new ResourceConverter(jsonObjectMapper, Battle.class);
        JSONAPIDocument<Battle> battle = resourceConverter.readDocument(battleUrl.openStream(), Battle.class);
        return battle.get();
    }

    @EventListener
    private void onPlanetUnderAssault(PlanetUnderAssaultMessage message) {
        val battle = getBattle(message.getBattleId());
        applicationEventPublisher.publishEvent(new NewBattleEvent(battle));
    }

    @EventListener
    private void onBattleWaitingProgressUpdate(BattleUpdateWaitingProgressMessage message) {
        applicationEventPublisher.publishEvent(new BattleUpdateWaitingProgressEvent(message.getBattleId(), message.getWaitingProgress()));
    }

}
