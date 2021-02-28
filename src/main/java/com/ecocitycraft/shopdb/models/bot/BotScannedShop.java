package com.ecocitycraft.shopdb.models.bot;

import com.ecocitycraft.shopdb.models.chestshops.Location;

public class BotScannedShop {
    private String id;
    private Location location;
    private String nameLine;
    private String quantityLine;
    private String priceLine;
    private String materialLine;
    private String regionName;

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public String getNameLine() {
        return nameLine;
    }

    public String getQuantityLine() {
        return quantityLine;
    }

    public String getPriceLine() {
        return priceLine;
    }

    public String getMaterialLine() {
        return materialLine;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setNameLine(String nameLine) {
        this.nameLine = nameLine;
    }

    public void setQuantityLine(String quantityLine) {
        this.quantityLine = quantityLine;
    }

    public void setPriceLine(String priceLine) {
        this.priceLine = priceLine;
    }

    public void setMaterialLine(String materialLine) {
        this.materialLine = materialLine;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
}
