package com.faforever.gw.services;

import com.faforever.gw.messaging.MessagingService;
import com.faforever.gw.messaging.incoming.*;
import com.faforever.gw.messaging.outgoing.*;
import com.faforever.gw.model.ClientState;
import com.faforever.gw.model.PlanetAction;
import com.faforever.gw.model.UniverseState;
import com.faforever.gw.model.entitity.*;
import com.faforever.gw.model.event.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GwClient {
    @Value("${gw.server.host}")
    private String host;
    @Value("${gw.server.port}")
    private int port;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessagingService messagingService;
    private final UniverseState universeState;
    private final UniverseApiAccessor api;
    private Map<UUID, ClientMessage> pendingMessages = new HashMap<>();
    private ClientState clientState;
    @Getter
    private UUID currentBattle;
    @Getter
    private GwCharacter myCharacter;

    @Inject
    public GwClient(ApplicationEventPublisher applicationEventPublisher, MessagingService messagingService, ObjectMapper jsonObjectMapper, UniverseState universeState, UniverseApiAccessor api) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.messagingService = messagingService;
        this.universeState = universeState;
        this.api = api;
    }

    private Battle getBattle(UUID id) {
        return universeState.getBattleFromCache(id)
                .orElse(loadBattle(id));
    }

    @EventListener
    private void onHello(HelloMessage message) {
        if (clientState == null || clientState == ClientState.DISCONNECTED) {
            loadUniverseFromApi();
        }

        clientState = ClientState.CONNECTED;

        if (message.getCharacterId() != null) {
            myCharacter = api.queryCharacter(message.getCharacterId().toString());
            universeState.addToCache(myCharacter);

            currentBattle = message.getCurrentBattleId();
            if (currentBattle == null) {
                clientState = ClientState.FREE_FOR_BATTLE;
            } else {
                clientState = ClientState.IN_ASSAULT;
            }
        }

        log.debug("New client state: {}", clientState);
        applicationEventPublisher.publishEvent(clientState);
        applicationEventPublisher.publishEvent(new UniverseLoadedEvent());
    }

    @SneakyThrows
    private void loadUniverseFromApi() {
        Map<String, SolarSystem> solarSystemDict = api.querySolarSystems().stream()
                .collect(Collectors.toMap(SolarSystem::getId, Function.identity()));

        Map<String, Planet> planetDict = solarSystemDict.values().stream()
                .flatMap(solarSystem -> solarSystem.getPlanets().stream())
                .collect(Collectors.toMap(Planet::getId, Function.identity()));


        Map<String, Battle> activeBattleDict = api.queryActiveBattles().stream()
                .collect(Collectors.toMap(Battle::getId, Function.identity()));

        universeState.init(solarSystemDict, planetDict, activeBattleDict);

        activeBattleDict.values().forEach(this::incorporate);

    }

    private Battle loadBattle(UUID id) {
        Battle battle = api.queryBattle(id.toString());
        incorporate(battle);
        return battle;
    }

    private GwCharacter loadCharacter(UUID id) {
        GwCharacter character = api.queryCharacter(id.toString());
        universeState.addToCache(character);
        return character;
    }

    /**
     * Incorporate the battle into the existing universe
     * (important: the planet object was created a second time, this removes the redundancy)
     *
     * @param battle
     */
    private void incorporate(Battle battle) {
        universeState.addToCache(battle);
        Planet planet = universeState.getPlanet(battle.getPlanet().getId());
        battle.setPlanet(planet);
        // attach the active battle to the planet
        if (planet.getBattles().stream()
                .noneMatch(planetBattle -> Objects.equals(planetBattle.getId(), battle.getId()))) {
            planet.getBattles().add(battle);
        }

        if ((battle.getStatus() == BattleStatus.INITIATED || battle.getStatus() == BattleStatus.RUNNING)
                && !universeState.getActiveBattleDict().containsKey(battle.getId())) {
            universeState.getActiveBattleDict().put(battle.getId(), battle);
        }
    }

    @EventListener
    private void onError(ErrorMessage message) {
        log.debug("Error from server: [{}] {}", message.getErrorCode(), message.getErrorMessage());
        applicationEventPublisher.publishEvent(new ErrorEvent(message.getErrorCode(), message.getErrorMessage()));
    }

    @EventListener
    private void onBattleParticipantJoined(BattleParticipantJoinedAssaultMessage message) {
        ClientState oldState = clientState;
        if (Objects.equals(message.getCharacterId().toString(), myCharacter.getId())) {
            clientState = ClientState.IN_ASSAULT;
            currentBattle = message.getBattleId();
        }

        Battle battle = getBattle(message.getBattleId());
        BattleParticipant participant = new BattleParticipant();
        GwCharacter character = universeState.getCharacterFromCache(message.getCharacterId())
                .orElse(loadCharacter(message.getCharacterId()));

        participant.setRole(battle.getAttackingFaction() == character.getFaction() ? BattleRole.ATTACKER : BattleRole.DEFENDER);
        participant.setBattle(battle);
        participant.setCharacter(character);

        battle.getParticipants().add(participant);

        applicationEventPublisher.publishEvent(new BattleChangedEvent(battle, false));

        if (oldState != clientState) {
            applicationEventPublisher.publishEvent(clientState);
        }
    }

    @EventListener
    private void onBattleParticipantLeft(BattleParticipantLeftAssaultMessage message) {
        ClientState oldState = clientState;
        if (Objects.equals(message.getCharacterId().toString(), myCharacter.getId())) {
            clientState = ClientState.FREE_FOR_BATTLE;
            currentBattle = null;
        }

        Battle battle = getBattle(message.getBattleId());
        battle.getParticipants().removeIf(battleParticipant ->
                Objects.equals(battleParticipant.getCharacter().getId(), message.getCharacterId().toString()));

        applicationEventPublisher.publishEvent(new BattleChangedEvent(battle, false));

        if (oldState != clientState) {
            applicationEventPublisher.publishEvent(clientState);
        }
    }

    @EventListener
    private void onPlanetConquered(PlanetConqueredMessage message) {
        battleEnded(message.getBattleId(), message.getAttackingFaction());
    }

    @EventListener
    private void onPlanetDefended(PlanetDefendedMessage message) {
        battleEnded(message.getBattleId(), message.getDefendingFaction());
    }

    private void battleEnded(UUID battleId, Faction winningFaction) {
        if (battleId.equals(currentBattle)) {
            currentBattle = null;
            clientState = ClientState.FREE_FOR_BATTLE;
            log.debug("Current battle - planet conquered -> New client state: {}", clientState);
            applicationEventPublisher.publishEvent(clientState);
        }

        Battle battle = getBattle(battleId);
        Planet planet = battle.getPlanet();

        battle.setStatus(BattleStatus.FINISHED);
        universeState.getActiveBattleDict().remove(battle.getId());
        planet.setCurrentOwner(winningFaction);

        applicationEventPublisher.publishEvent(new BattleChangedEvent(battle, false));
        applicationEventPublisher.publishEvent(new PlanetOwnerChangedEvent(planet, winningFaction));
    }

    public void connect(String token) {
        String uri = String.format("ws://%s:%s/websocket?access_token=%s", host, port, token);
        messagingService.connect(uri);
    }

    public void disconnect() {
        messagingService.disconnect();
        clientState = ClientState.DISCONNECTED;
        log.debug("New client state: {}", clientState);
        applicationEventPublisher.publishEvent(clientState);
    }

    public void requestCharacter(Faction faction) throws IOException {
        messagingService.send(new RequestCharacterMessage(faction));
    }

    public void selectName(UUID requestId, String name) throws IOException {
        messagingService.send(new SelectCharacterNameMessage(requestId, name));
    }

    public void initiateAssault(UUID planetId) throws IOException {
        messagingService.send(new InitiateAssaultMessage(planetId));
    }

    public void joinAssault(String battleId) throws IOException {
        joinAssault(UUID.fromString(battleId));
    }

    public void joinAssault(UUID battleId) throws IOException {
        messagingService.send(new JoinAssaultMessage(battleId));
    }

    public void leaveAssault() throws IOException {
        messagingService.send(new LeaveAssaultMessage(currentBattle));
    }

    @EventListener
    private void onPlanetUnderAssault(PlanetUnderAssaultMessage message) {
        val battle = getBattle(message.getBattleId());
        applicationEventPublisher.publishEvent(new BattleChangedEvent(battle, true));
    }

    @EventListener
    private void onBattleWaitingProgressUpdate(BattleUpdateWaitingProgressMessage message) {
        Battle battle = getBattle(message.getBattleId());
        battle.setWaitingProgress(message.getWaitingProgress());
        applicationEventPublisher.publishEvent(new BattleChangedEvent(battle, false));
    }


    public PlanetAction getPossibleActionFor(Planet planet) {
        Faction myFaction = getMyCharacter().getFaction();

        Optional<Battle> battleOptional = universeState.getActiveBattleForPlanet(planet.getId());

        if (battleOptional.isPresent()) {
            Battle battle = battleOptional.get();

            if (battle.getStatus() == BattleStatus.INITIATED) {
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
        }

        if (planet.getCurrentOwner() != myFaction) {
            // TODO: Check for planet in reach
            return PlanetAction.START_ASSAULT;
        }

        // it's your factions planet - no action
        return PlanetAction.NONE;
    }

    @EventListener
    public void onCharacterNameProposal(CharacterNameProposalMessage message) {
        applicationEventPublisher.publishEvent(new CharacterNameProposalEvent(message.getRequestId(), message.getProposedNamesList()));
    }

    @EventListener
    private void onSolarSystemsLinked(SolarSystemsLinkedMessage message) {
        SolarSystem from = universeState.getSolarSystem(message.getSolarSystemFrom());
        SolarSystem to = universeState.getSolarSystem(message.getSolarSystemTo());

        log.debug("Solar systems link established between {} to {}", from, to);
        from.getConnectedSystems().add(to);
        to.getConnectedSystems().add(from);
        applicationEventPublisher.publishEvent(new SolarSystemsLinkedEvent(from, to));
    }

    @EventListener
    private void onSolarSystemsUnlinked(SolarSystemsUnlinkedMessage message) {
        SolarSystem from = universeState.getSolarSystem(message.getSolarSystemFrom());
        SolarSystem to = universeState.getSolarSystem(message.getSolarSystemTo());

        log.debug("Solar systems link removed between {} to {}", from, to);
        from.getConnectedSystems().remove(to);
        to.getConnectedSystems().remove(from);
        applicationEventPublisher.publishEvent(new SolarSystemsUnlinkedEvent(from, to));
    }

    @EventListener
    private void onSetPlanetFaction(PlanetOwnerChangedMessage message) {
        Planet planet = universeState.getPlanet(message.getPlanetId());
        Faction newOwner = message.getNewOwner();

        log.debug("Changing owner for planet {} to {}", planet, newOwner);
        planet.setCurrentOwner(newOwner);
        applicationEventPublisher.publishEvent(new PlanetOwnerChangedEvent(planet, newOwner));
    }

    public void adminLinkSolarSystems(SolarSystem from, SolarSystem to) throws IOException {
        messagingService.send(new LinkSolarSystemsRequestMessage(UUID.fromString(from.getId()), UUID.fromString(to.getId())));
    }

    public void adminUnlinkSolarSystems(SolarSystem from, SolarSystem to) throws IOException {
        messagingService.send(new UnlinkSolarSystemsRequestMessage(UUID.fromString(from.getId()), UUID.fromString(to.getId())));
    }

    @SneakyThrows
    public void setFaction(Planet planet, Faction selectedFaction) {
        log.debug("Requesting change of planet {} -> set owner to {}", planet, selectedFaction);
        messagingService.send(new SetPlanetFactionRequestMessage(UUID.fromString(planet.getId()), selectedFaction));
    }

    @SneakyThrows
    public void sendDebug(String action) {
        messagingService.send(new DebugMessage((action)));
    }
}

