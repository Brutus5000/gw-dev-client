package com.faforever.gw.model.entitity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

@Data
@Type("solarSystem")
public class SolarSystem implements Serializable {
    @Id
    private String id;
    private String name;
    private long x;
    private long y;
    private long z;

    @Relationship("planets")
    private List<Planet> planets;

    @Relationship("connectedSystems")
    private List<SolarSystem> connectedSystems;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SolarSystem)) return false;
        return Objects.equals(id, ((SolarSystem) o).getId());
    }

    public String toString() {
        return MessageFormat.format("SolarSystem @ ({0},{1},{2}) [ID = {3}]", x, y, z, id);
    }

    @JsonIgnore
    public Faction getUniqueOwner() {
        Faction uniqueFaction = null;

        boolean first = true;

        for (Planet p : planets) {
            if (first) {
                first = false;
                uniqueFaction = p.getCurrentOwner();
            } else {
                if (uniqueFaction != p.getCurrentOwner())
                    return null;
            }
        }

        return uniqueFaction;
    }
}
