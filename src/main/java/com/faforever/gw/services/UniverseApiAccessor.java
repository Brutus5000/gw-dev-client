package com.faforever.gw.services;

import com.faforever.gw.model.entitity.*;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UniverseApiAccessor {
    private final ResourceConverter resourceConverter;

    private Map<String, SolarSystem> solarSystemDict;
    private Map<String, Planet> planetDict;
    private Map<String, Battle> battleDict;
    @Getter
    private Map<String, Battle> activeBattles;
    @Value("${gw.server.host}")
    private String host;
    @Value("${gw.server.port}")
    private int port;
    @Value("${gw.server.protocol}")
    private String protocol;

    @Inject
    public UniverseApiAccessor() {
        this.resourceConverter = new ResourceConverter(SolarSystem.class, Planet.class, Battle.class, BattleParticipant.class, GwCharacter.class);
    }

    @SneakyThrows
    private URL buildURL(String query) {
        return new URL(MessageFormat.format("{0}://{1}:{2,number,#}/data/{3}", protocol, host, port, query));
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
        URL solarSystemListUrl = buildURL("solarSystem?include=planets");
        JSONAPIDocument<List<SolarSystem>> solarSystemList = resourceConverter.readDocumentCollection(solarSystemListUrl.openStream(), SolarSystem.class);

        return solarSystemList.get();
    }

    @SneakyThrows
    private List<Battle> queryActiveBattles() {
        URL battleListUrl = buildURL("battle?include=participants,participants.character&filter[battle]=status=in=('INITIATED','RUNNING')");
        JSONAPIDocument<List<Battle>> battleList = resourceConverter.readDocumentCollection(battleListUrl.openStream(), Battle.class);

        return battleList.get();
    }

    @SneakyThrows
    private Battle queryBattle(String id) {
        URL battleUrl = buildURL(MessageFormat.format("battle/{0}?include=participants,participants.character", id));
        JSONAPIDocument<Battle> battle = resourceConverter.readDocument(battleUrl.openStream(), Battle.class);

        return battle.get();
    }

    @SneakyThrows
    private GwCharacter queryCharacter(String id) {
        URL characterUrl = buildURL(MessageFormat.format("gwCharacter/{0}", id));
        JSONAPIDocument<GwCharacter> character = resourceConverter.readDocument(characterUrl.openStream(), GwCharacter.class);

        return character.get();
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

    public GwCharacter getCharacter(UUID id) {
        return queryCharacter(id.toString());
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

    public Optional<Battle> getActiveBattleForPlanet(String id) {
        Planet planet = getPlanet(id);

        for (Battle battle : planet.getBattles()) {
            if (activeBattles.containsKey(battle.getId())) {
                return Optional.of(battle);
            }
        }

        return Optional.empty();
    }
}
