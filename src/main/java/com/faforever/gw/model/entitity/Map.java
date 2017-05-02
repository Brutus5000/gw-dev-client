package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Type("map")
public class Map {
    @Id
    private String id;
    private Integer fafMapId;
    private Integer fafMapVersion;
    private Integer totalSlots;
    private Integer size;
    private Ground ground;
    private List<Planet> planetList = new ArrayList<>();
}
