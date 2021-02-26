package com.ecocitycraft.shopdb.models.regions;

import com.ecocitycraft.shopdb.models.chestshops.Location;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class RegionRequest {
    private String name;
    private String server;
    private Location iBounds;
    private Location oBounds;
    @JsonProperty("owners")
    private Set<String> mayorNames;
    private Boolean active = false;

    public String getName() {
        return name;
    }

    public String getServer() {
        return server;
    }

    public Location getiBounds() {
        return iBounds;
    }

    public Location getoBounds() {
        return oBounds;
    }

    public Set<String> getMayorNames() {
        return mayorNames;
    }

    public Boolean getActive() {
        return active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setiBounds(Location iBounds) {
        this.iBounds = iBounds;
    }

    public void setoBounds(Location oBounds) {
        this.oBounds = oBounds;
    }

    public void setMayorNames(Set<String> mayorNames) {
        this.mayorNames = mayorNames;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
