package com.faforever.gw.services;

import com.faforever.gw.messaging.MessagingService;
import com.faforever.gw.messaging.incoming.*;
import com.faforever.gw.messaging.outgoing.ClientMessage;
import com.faforever.gw.messaging.outgoing.InitiateAssaultMessage;
import com.faforever.gw.messaging.outgoing.JoinAssaultMessage;
import com.faforever.gw.messaging.outgoing.LeaveAssaultMessage;
import com.faforever.gw.model.ClientState;
import com.faforever.gw.model.PlanetAction;
import com.faforever.gw.model.entitity.Battle;
import com.faforever.gw.model.entitity.Faction;
import com.faforever.gw.model.entitity.GwCharacter;
import com.faforever.gw.model.entitity.Planet;
import com.faforever.gw.model.event.BattleUpdateWaitingProgressEvent;
import com.faforever.gw.model.event.ErrorEvent;
import com.faforever.gw.model.event.NewBattleEvent;
import com.faforever.gw.model.event.PlanetDefendedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class GwClient {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessagingService messagingService;
    private final UniverseApiAccessor universeApiAccessor;
    private Map<UUID, ClientMessage> pendingMessages = new HashMap<>();
    private ClientState clientState;
    @Getter
    private UUID currentBattle;
    @Getter
    private GwCharacter myCharacter;

    @Inject
    public GwClient(ApplicationEventPublisher applicationEventPublisher, MessagingService messagingService, ObjectMapper jsonObjectMapper, UniverseApiAccessor universeApiAccessor) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.messagingService = messagingService;
        this.universeApiAccessor = universeApiAccessor;
    }

    @EventListener
    private void onHello(HelloMessage message) {
        myCharacter = universeApiAccessor.getCharacter(message.getCharacterId());
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
    private void onError(ErrorMessage message) {
        log.debug("Error from server: [{}] {}", message.getErrorCode(), message.getErrorMessage());
        applicationEventPublisher.publishEvent(new ErrorEvent(message.getErrorCode(), message.getErrorMessage()));
    }

    @EventListener
    private void onBattleParticipantJoined(BattleParticipantJoinedAssaultMessage message) {
        universeApiAccessor.update();
        if (message.getCharacterId().equals(myCharacter)) {
            clientState = ClientState.IN_ASSAULT;
            currentBattle = message.getBattleId();
            applicationEventPublisher.publishEvent(clientState);
        }
    }

    @EventListener
    private void onBattleParticipantLeft(BattleParticipantLeftAssaultMessage message) {
        universeApiAccessor.update();
        if (message.getCharacterId().equals(myCharacter)) {
            clientState = ClientState.FREE_FOR_BATTLE;
            currentBattle = null;
            applicationEventPublisher.publishEvent(clientState);
        }
    }

    @EventListener
    private void onPlanetConquered(PlanetConqueredMessage message) {
        universeApiAccessor.update();
        if (message.getBattleId().equals(currentBattle)) {
            clientState = ClientState.FREE_FOR_BATTLE;
            log.debug("Current battle - planet conquered -> New client state: {}", clientState);
            applicationEventPublisher.publishEvent(clientState);
        }

        applicationEventPublisher.publishEvent(new PlanetDefendedEvent(universeApiAccessor.getBattle(message.getBattleId())));
    }

    @EventListener
    private void onPlanetDefended(PlanetDefendedMessage message) {
        universeApiAccessor.update();
        if (message.getBattleId().equals(currentBattle)) {
            clientState = ClientState.FREE_FOR_BATTLE;
            log.debug("Current battle - planet defended -> New client state: {}", clientState);
            applicationEventPublisher.publishEvent(clientState);
        }

        applicationEventPublisher.publishEvent(new PlanetDefendedEvent(universeApiAccessor.getBattle(message.getBattleId())));
    }

    public void connect(String host, int port, String token) {
        String uri = String.format("ws://%s:%s/websocket?accessToken=%s", host, port, token);
        messagingService.connect(uri);
    }

    public void disconnect() {
        messagingService.disconnect();
        clientState = ClientState.DISCONNECTED;
        log.debug("New client state: {}", clientState);
        applicationEventPublisher.publishEvent(clientState);
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
        messagingService.send(new LeaveAssaultMessage(currentBattle));
    }

    @EventListener
    private void onPlanetUnderAssault(PlanetUnderAssaultMessage message) {
        universeApiAccessor.update();
        val battle = universeApiAccessor.getBattle(message.getBattleId());
        applicationEventPublisher.publishEvent(new NewBattleEvent(battle));
    }

    @EventListener
    private void onBattleWaitingProgressUpdate(BattleUpdateWaitingProgressMessage message) {
        applicationEventPublisher.publishEvent(new BattleUpdateWaitingProgressEvent(message.getBattleId(), message.getWaitingProgress()));
    }


    public PlanetAction getPossibleActionFor(Planet planet) {
        Faction myFaction = getMyCharacter().getFaction();

        Optional<Battle> battleOptional = universeApiAccessor.getActiveBattleForPlanet(planet.getId());

        if (battleOptional.isPresent()) {
            Battle battle = battleOptional.get();

            if (getCurrentBattle() == null) {
                if (battle.getAttackingFaction() == myFaction) {
                    // TODO: Check for open slots
                    return PlanetAction.JOIN_OFFENSE;
                }

                if (battle.getDefendingFaction() == myFaction) {
                    // TODO: Check for open slots
                    return PlanetAction.JOIN_DEFENSE;
                }

                // your faction is not involved in this battle
                return PlanetAction.NONE;
            }

            if (Objects.equals(battle.getId(), getCurrentBattle().toString())) {
                return PlanetAction.LEAVE;
            }

            // you can't do anything else until you leave the current battle (which is somewhere else)
            return PlanetAction.NONE;

        }

        if (planet.getCurrentOwner() != myFaction) {
            // TODO: Check for planet in reach
            return PlanetAction.START_ASSAULT;
        }

        // it's your factions planet - no action
        return PlanetAction.NONE;
    }
}

