package com.faforever.gw.model;

import com.faforever.gw.model.entitity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UniverseApiAccessor {
    private final ObjectMapper jsonObjectMapper;
    private final ResourceConverter resourceConverter;

    private Map<String, SolarSystem> solarSystemDict;
    private Map<String, Planet> planetDict;
    private Map<String, Battle> battleDict;
    @Getter
    private Map<String, Battle> activeBattles;
    private String host;
    private int port;

    @Inject
    public UniverseApiAccessor(ObjectMapper jsonObjectMapper) {
        this.jsonObjectMapper = jsonObjectMapper;
        this.resourceConverter = new ResourceConverter(SolarSystem.class, Planet.class, Battle.class, BattleParticipant.class, GwCharacter.class);
    }

    public void connect(String host, int port) {
        this.host = host;
        this.port = port;
        update();
    }

    public void disconnect() {
        solarSystemDict.clear();
        planetDict.clear();
        battleDict.clear();
        activeBattles.clear();
    }

    public void update() {
        buildUniverse();
    }

    @SneakyThrows
    private void buildUniverse() {
        solarSystemDict = querySolarSystems().stream()
                .collect(Collectors.toMap(SolarSystem::getId, Function.identity()));

        planetDict = solarSystemDict.values().stream()
                .flatMap(solarSystem -> solarSystem.getPlanets().stream())
                .collect(Collectors.toMap(Planet::getId, Function.identity()));


        activeBattles = queryActiveBattles().stream()
                .collect(Collectors.toMap(Battle::getId, Function.identity()));
        activeBattles.values().forEach(this::incorporate);

        // we don't query all battles at once, since these are too many
        // only the active = interesting ones are queried
        battleDict = new HashMap<>(activeBattles);
    }

    @SneakyThrows
    private List<SolarSystem> querySolarSystems() {
        URL solarSystemListUrl = new URL(String.format("http://%s:%s/data/solarSystem?include=planets", host, port));
        JSONAPIDocument<List<SolarSystem>> solarSystemList = resourceConverter.readDocumentCollection(solarSystemListUrl.openStream(), SolarSystem.class);

        return solarSystemList.get();
    }

    @SneakyThrows
    private List<Battle> queryActiveBattles() {
        URL battleListUrl = new URL(String.format("http://%s:%s/data/battle?include=participants,participants.character&filter[battle]=status=in=('INITIATED','RUNNING')", host, port));
        JSONAPIDocument<List<Battle>> battleList = resourceConverter.readDocumentCollection(battleListUrl.openStream(), Battle.class);

        return battleList.get();
    }

    @SneakyThrows
    private Battle queryBattle(String id) {
        URL battleUrl = new URL(String.format("http://%s:%s/data/battle/%s?include=participants,participants.character", host, port, id));
        JSONAPIDocument<Battle> battle = resourceConverter.readDocument(battleUrl.openStream(), Battle.class);

        return battle.get();
    }

    public Collection<SolarSystem> getSolarSystems() {
        return solarSystemDict.values();
    }

    public SolarSystem getSolarSystem(String id) {
        return solarSystemDict.get(id);
    }

    public Collection<Planet> getPlanets() {
        return planetDict.values();
    }

    public Planet getPlanet(String id) {
        return planetDict.get(id);
    }

    public Battle getBattle(UUID id) {
        return getBattle(id.toString());
    }

    public Battle getBattle(String id) {
        if (battleDict.containsKey(id)) {
            return battleDict.get(id);
        }

        Battle battle = queryBattle(id);
        incorporate(battle);

        return battle;
    }

    /**
     * Incorporate the battle into the existing universe
     * (important: the planet object was created a second time, this removes the redundancy)
     *
     * @param battle
     */
    private void incorporate(Battle battle) {
        Planet planet = planetDict.get(battle.getPlanet().getId());
        battle.setPlanet(planet);
        // attach the active battle to the planet
        if (planet.getBattles().stream()
                .noneMatch(planetBattle -> Objects.equals(planetBattle.getId(), battle.getId()))) {
            planet.getBattles().add(battle);
        }

        if ((battle.getStatus() == BattleStatus.INITIATED || battle.getStatus() == BattleStatus.RUNNING)
                && !activeBattles.containsKey(battle.getId())) {
            activeBattles.put(battle.getId(), battle);
        }
    }
}
