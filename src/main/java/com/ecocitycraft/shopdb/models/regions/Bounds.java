package com.ecocitycraft.shopdb.models.regions;

import com.ecocitycraft.shopdb.models.chestshops.Location;

public class Bounds {
    private Location lowerBounds;
    private Location upperBounds;

    public Bounds() {
    }

    public Bounds(Location lowerBounds, Location upperBounds) {
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
    }

    public Location getLowerBounds() {
        return lowerBounds;
    }

    public Location getUpperBounds() {
        return upperBounds;
    }

    public void setLowerBounds(Location lowerBounds) {
        this.lowerBounds = lowerBounds;
    }

    public void setUpperBounds(Location upperBounds) {
        this.upperBounds = upperBounds;
    }
}
