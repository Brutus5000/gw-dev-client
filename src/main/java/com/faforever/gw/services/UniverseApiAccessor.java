package com.faforever.gw.services;

import com.faforever.gw.model.entitity.*;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

@Service
public class UniverseApiAccessor {
    private final ResourceConverter resourceConverter;

    @Value("${gw.server.host}")
    private String host;
    @Value("${gw.server.port}")
    private int port;
    @Value("${gw.server.protocol}")
    private String protocol;

    @Inject
    public UniverseApiAccessor() {
        this.resourceConverter = new ResourceConverter(SolarSystem.class, Planet.class, Battle.class, BattleParticipant.class, GwCharacter.class, Reinforcement.class, Unit.class, PassiveItem.class);
    }

    @SneakyThrows
    private URL buildDataURL(String query) {
        return new URL(MessageFormat.format("{0}://{1}:{2,number,#}/data/{3}", protocol, host, port, query));
    }

    @SneakyThrows
    public List<SolarSystem> querySolarSystems() {
        URL solarSystemListUrl = buildDataURL("solarSystem?include=connectedSystems,planets");
        JSONAPIDocument<List<SolarSystem>> solarSystemList = resourceConverter.readDocumentCollection(solarSystemListUrl.openStream(), SolarSystem.class);

        return solarSystemList.get();
    }

    @SneakyThrows
    public List<Battle> queryActiveBattles() {
        URL battleListUrl = buildDataURL("battle?include=participants,participants.character&filter[battle]=status=in=('INITIATED','RUNNING')");
        JSONAPIDocument<List<Battle>> battleList = resourceConverter.readDocumentCollection(battleListUrl.openStream(), Battle.class);

        return battleList.get();
    }

    @SneakyThrows
    public Battle queryBattle(String id) {
        URL battleUrl = buildDataURL(MessageFormat.format("battle/{0}?include=participants,participants.character", id));
        JSONAPIDocument<Battle> battle = resourceConverter.readDocument(battleUrl.openStream(), Battle.class);

        return battle.get();
    }

    @SneakyThrows
    public GwCharacter queryCharacter(String id) {
        URL characterUrl = buildDataURL(MessageFormat.format("gwCharacter/{0}", id));
        JSONAPIDocument<GwCharacter> character = resourceConverter.readDocument(characterUrl.openStream(), GwCharacter.class);

        return character.get();
    }

    @SneakyThrows
    public List<Reinforcement> queryReinforcements() {
        URL reinforcementListUrl = buildDataURL("reinforcement?include=unit,item");
        JSONAPIDocument<List<Reinforcement>> reinforcementList = resourceConverter.readDocumentCollection(reinforcementListUrl.openStream(), Reinforcement.class);

        return reinforcementList.get();
    }
}
