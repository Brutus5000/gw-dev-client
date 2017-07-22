package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

}
