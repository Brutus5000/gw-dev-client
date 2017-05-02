package com.faforever.gw.model.entitity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Rank implements Serializable {
    Long xpMin;
    private Integer level;
    private List<GwCharacter> characters;
    private String uefTitle;
    private String aeonTitle;
    private String cybranTitle;
    private String seraphimTitle;

    public String getTitle(Faction faction) {
        switch (faction) {
            case UEF:
                return uefTitle;
            case AEON:
                return aeonTitle;
            case CYBRAN:
                return cybranTitle;
            case SERAPHIM:
                return seraphimTitle;
        }

        throw new RuntimeException("This code is not allowed to be reached");
    }

}
