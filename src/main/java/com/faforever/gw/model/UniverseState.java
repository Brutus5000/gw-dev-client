package com.faforever.gw.model;

import com.faforever.gw.model.entitity.Battle;
import com.faforever.gw.model.entitity.GwCharacter;
import com.faforever.gw.model.entitity.Planet;
import com.faforever.gw.model.entitity.SolarSystem;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service
public class UniverseState {
    private final ApplicationEventPublisher applicationEventPublisher;

    private Map<String, SolarSystem> solarSystemDict;
    private Map<String, Planet> planetDict;
    private Map<String, Battle> activeBattleDict;
    private Map<String, Battle> battleCache;
    private Map<String, GwCharacter> characterCache;

    @Inject
    public UniverseState(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void init(
            Map<String, SolarSystem> solarSystemDict,
            Map<String, Planet> planetDict,
            Map<String, Battle> activeBattleDict
    ) {
        this.solarSystemDict = solarSystemDict;
        this.planetDict = planetDict;
        this.activeBattleDict = activeBattleDict;
        this.battleCache = new HashMap<>(activeBattleDict);
        this.characterCache = new HashMap<>();
    }

    public Collection<SolarSystem> getSolarSystems() {
        return solarSystemDict.values();
    }

    public SolarSystem getSolarSystem(UUID id) {
        return getSolarSystem(id.toString());
    }

    public SolarSystem getSolarSystem(String id) {
        return solarSystemDict.get(id);
    }

    public Collection<Planet> getPlanets() {
        return planetDict.values();
    }

    public Planet getPlanet(UUID id) {
        return getPlanet(id.toString());
    }

    public Planet getPlanet(String id) {
        return planetDict.get(id);
    }

    public void addToCache(Battle battle) {
        battleCache.putIfAbsent(battle.getId(), battle);
    }

    public Optional<Battle> getBattleFromCache(UUID id) {
        return getBattleFromCache(id.toString());
    }

    public Optional<Battle> getBattleFromCache(String id) {
        return Optional.ofNullable(battleCache.get(id));
    }

    public Map<String, Battle> getActiveBattleDict() {
        return activeBattleDict;
    }

    public void addToCache(GwCharacter character) {
        characterCache.putIfAbsent(character.getId(), character);
    }

    public Optional<GwCharacter> getCharacterFromCache(UUID id) {
        return getCharacterFromCache(id.toString());
    }

    public Optional<GwCharacter> getCharacterFromCache(String id) {
        return Optional.ofNullable(characterCache.get(id));
    }

    public Optional<Battle> getActiveBattleForPlanet(String id) {
        Planet planet = getPlanet(id);

        for (Battle battle : planet.getBattles()) {
            if (activeBattleDict.containsKey(battle.getId())) {
                return Optional.of(battle);
            }
        }

        return Optional.empty();
    }
}
