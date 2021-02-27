package com.ecocitycraft.shopdb.models.players;

import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerRegionDto {
    private String name;
    private Server server;

    public String getName() {
        return name;
    }

    public Server getServer() {
        return server;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
