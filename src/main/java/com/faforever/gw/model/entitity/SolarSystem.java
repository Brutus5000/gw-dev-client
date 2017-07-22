package com.faforever.gw.model.entitity;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Type("solarSystem")
public class SolarSystem implements Serializable {
    @Id
    private String id;
    private long x;
    private long y;
    private long z;

    @Relationship("planets")
    private List<Planet> planets;
}
