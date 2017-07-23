package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Type("planet")
public class Planet implements Serializable {
    @Id
    private String id;
    @Relationship("solarSystem")
    private SolarSystem solarSystem;
    private List<Battle> battles = new ArrayList<>();
    private int orbitLevel;
    private int size;
    private boolean habitable;
    private Ground ground;
    //    private Map map;
    private Faction currentOwner;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Planet)) return false;
        return Objects.equals(id, ((Planet) o).getId());
    }

    public String toString() {
        return MessageFormat.format("Planet [ID = {0}]", id);
    }
}
