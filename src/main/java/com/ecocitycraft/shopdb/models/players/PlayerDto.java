package com.ecocitycraft.shopdb.models.players;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerDto {
    private String name;
    private Timestamp lastSeen;
    private Timestamp lastUpdated;
    private int numChestShops;
    private List<PlayerRegionDto> towns;

    public String getName() {
        return name;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PlayerRegionDto> getTowns() {
        return towns;
    }

    public int getNumChestShops() {
        return numChestShops;
    }

    public void setLastSeen(Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setTowns(List<PlayerRegionDto> towns) {
        this.towns = towns;
    }

    public void setNumChestShops(int numChestShops) {
        this.numChestShops = numChestShops;
    }
}
