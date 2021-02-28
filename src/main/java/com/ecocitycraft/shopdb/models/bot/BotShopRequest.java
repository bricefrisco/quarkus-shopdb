package com.ecocitycraft.shopdb.models.bot;

import com.ecocitycraft.shopdb.models.chestshops.Server;

import java.util.List;

public class BotShopRequest {
    private Server server;
    private String regionName;
    private List<BotScannedShop> signs;

    public Server getServer() {
        return server;
    }

    public String getRegionName() {
        return regionName;
    }

    public List<BotScannedShop> getSigns() {
        return signs;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setSigns(List<BotScannedShop> signs) {
        this.signs = signs;
    }
}
