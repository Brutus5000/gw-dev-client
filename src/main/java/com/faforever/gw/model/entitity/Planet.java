package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

@Data
@Type("planet")
public class Planet {
    @Id
    private String id;
    //    private List<Battle> battles;
    private int orbitLevel;
    private int size;
    private boolean habitable;
    private Ground ground;
    //    private Map map;
    private Faction currentOwner;
}
