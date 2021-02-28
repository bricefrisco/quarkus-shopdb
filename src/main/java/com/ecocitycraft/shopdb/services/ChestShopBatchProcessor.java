package com.ecocitycraft.shopdb.services;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.models.chestshops.EventType;
import com.ecocitycraft.shopdb.models.chestshops.Location;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.ecocitycraft.shopdb.models.chestshops.ShopEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class ChestShopBatchProcessor {
    final Logger LOGGER = LoggerFactory.getLogger(ChestShopBatchProcessor.class);

    public String createChestShopSigns(List<ShopEvent> shopEvents) {
        List<String> shopIdsToDelete = new ArrayList<>();
        List<ShopEvent> upserts = new ArrayList<>();
        Set<String> playerNames = new HashSet<>();

        LOGGER.info("Sorting shop events...");
        for (ShopEvent shopEvent : shopEvents) {
            if (shopEvent.getEventType().equals(EventType.DELETE)) {
                shopIdsToDelete.add(shopEvent.getId());
            } else {
                upserts.add(shopEvent);
                playerNames.add(shopEvent.getOwner().toLowerCase(Locale.ROOT));
            }
        }

        LOGGER.info("Found " + shopIdsToDelete.size() + " shop deletion events.");
        LOGGER.info("Found " + upserts.size() + " shops to create or modify.");

        for (String id : shopIdsToDelete) {
            ChestShop.deleteById(id);
        }

        if (upserts.size() == 0) {
            String response = "Successfully created or updated 0 chest shops, and removed " + shopIdsToDelete.size() + " chest shops.";
            LOGGER.info(response);
            return response;
        }

        LOGGER.info("Retrieving/adding " + playerNames.size() + " owners...");
        HashMap<String, Player> players = Player.getOrAddPlayers(playerNames);

        LOGGER.info("Mapping " + shopEvents.size() + " events to chest shops...");
        List<ChestShop> chestShops = new ArrayList<>();
        for (ShopEvent upsert : upserts) {
            if (!eventIsValid(upsert)) continue;
            Optional<ChestShop> maybeChestShop = ChestShop.findByIdOptional(upsert.getId());
            ChestShop chestShop = maybeChestShop.map(shop -> convert(shop, upsert, players)).orElseGet(() -> convert(upsert, players));
            chestShops.add(chestShop);
        }

        LOGGER.info("Adding " + chestShops.size() + " chest shops...");
        if (chestShops.size() > 0) {
            ChestShop.persist(chestShops);
        }

        String response = "Successfully created or updated " + chestShops.size() + " chest shops, and removed " + shopIdsToDelete.size() + " chest shops.";
        LOGGER.info(response);
        return response;
    }

    public void linkAndShowChestShops(Region region) {
        List<ChestShop> shops = ChestShop.findInRegion(region);
        for (ChestShop shop : shops) {
            shop.town = region;
            shop.isHidden = Boolean.FALSE;
        }
        ChestShop.persist(shops);
    }

    public void linkAndHideChestShops(Region region) {
        List<ChestShop> shops = ChestShop.findInRegion(region);
        for (ChestShop shop : shops) {
            shop.town = region;
            shop.isHidden = Boolean.TRUE;
        }
        ChestShop.persist(shops);
    }

    private boolean eventIsValid(ShopEvent event) {
        if (event.getId() == null || event.getId().isEmpty()) {
            LOGGER.info("Skipping event " + event.toString() + " - ID is null or empty.");
            return false;
        }

        if (event.getEventType() == null) {
            LOGGER.info("Skipping event " + event.toString() + " - event type not specified.");
            return false;
        }

        if (event.getWorld() == null || event.getWorld().isEmpty()) {
            LOGGER.info("Skipping event " + event.toString() + " - no world specified.");
            return false;
        }

        if (!event.getWorld().equals("rising") && !event.getWorld().equals("rising_n") && !event.getWorld().equals("rising_e")) {
            LOGGER.info("Skipping event " + event.toString() + " - server cannot be determined.");
            return false;
        }

        if (event.getX() == null || event.getY() == null || event.getZ() == null) {
            LOGGER.info("Skipping event " + event.toString() + " - X, Y, or Z coordinate is missing.");
            return false;
        }

        if (event.getOwner() == null || event.getOwner().isEmpty()) {
            LOGGER.info("Skipping event " + event.toString() + " - owner is missing");
            return false;
        }

        if (event.getQuantity() == null || event.getQuantity() == 0) {
            LOGGER.info("Skipping event " + event.toString() + " - shop quantity is missing");
            return false;
        }

        if (event.getCount() == null) {
            LOGGER.info("Skipping event " + event.toString() + " - count is missing");
            return false;
        }

        if (event.getItem() == null || event.getItem().isEmpty()) {
            LOGGER.info("Skipping event " + event.toString() + " - item is missing");
            return false;
        }

        if (event.getFull() == null) {
            LOGGER.info("Skipping event " + event.toString() + " - 'full' indicator is missing");
            return false;
        }

        return true;
    }

    private ChestShop convert(ShopEvent event, HashMap<String, Player> players) {
        ChestShop chestShop = new ChestShop();
        chestShop.id = event.getId();

        String world = event.getWorld();
        switch (world) {
            case "rising":
                chestShop.server = Server.MAIN;
                break;
            case "rising_n":
                chestShop.server = Server.MAIN_NORTH;
                break;
            case "rising_e":
                chestShop.server = Server.MAIN_EAST;
                break;
        }

        Location location = new Location();
        location.setX(event.getX());
        location.setY(event.getY());
        location.setZ(event.getZ());
        chestShop.location = location;

        return convert(chestShop, event, players);
    }

    private ChestShop convert(ChestShop sign, ShopEvent event, HashMap<String, Player> players) {
        sign.owner = players.get(event.getOwner().toLowerCase(Locale.ROOT));
        sign.quantity = event.getQuantity();
        sign.quantityAvailable = event.getCount();

        if (event.getSellPrice() != null && event.getSellPrice().doubleValue() != -1.0) {
            sign.sellPrice = event.getSellPrice().doubleValue();
            sign.sellPriceEach = determineSellPriceEach(event.getQuantity(), event.getSellPrice().doubleValue());
            sign.isSellSign = Boolean.TRUE;
        } else {
            sign.sellPrice = null;
            sign.sellPriceEach = null;
            sign.isSellSign = Boolean.FALSE;
        }

        if (event.getBuyPrice() != null && event.getBuyPrice().doubleValue() != -1.0) {
            sign.buyPrice = event.getBuyPrice().doubleValue();
            sign.buyPriceEach = determineBuyPriceEach(event.getQuantity(), event.getBuyPrice().doubleValue());
            sign.isBuySign = Boolean.TRUE;
        } else {
            sign.buyPrice = null;
            sign.buyPriceEach = null;
            sign.isBuySign = Boolean.FALSE;
        }

        if (sign.town == null) {
            List<Region> regions = Region.findByCoordinates(event.getX(), event.getY(), event.getZ(), sign.server);
            if (regions != null && regions.size() > 0) {
                if (regions.size() > 1) {
                    LOGGER.warn("Conflicting regions for event: " + event.toString());

                    Region selectedRegion = null;
                    StringBuilder sb = new StringBuilder();
                    for (Region region : regions) {
                        sb.append(region.name).append(",");
                        if (region.active) {
                            selectedRegion = region;
                        }
                    }

                    LOGGER.warn("Regions are (server: " + sign.server + "): " + sb.toString());

                    if (selectedRegion != null) {
                        LOGGER.warn("Selected region is (server: " + sign.server + "): " + selectedRegion.name);
                        sign.town = selectedRegion;
                    } else {
                        sign.town = regions.get(0);
                    }
                } else {
                    sign.town = regions.get(0);
                }
            }
        }

        sign.material = event.getItem().toLowerCase(Locale.ROOT);

        sign.isHidden = sign.town == null || !sign.town.active;
        sign.isFull = event.getFull();
        sign.isSellSign = sign.sellPrice != null;

        return sign;
    }

    private Double determineSellPriceEach(Integer quantity, Double sellPrice) {
        return quantity == null || sellPrice == null ? null : sellPrice / quantity;
    }

    private Double determineBuyPriceEach(Integer quantity, Double buyPrice) {
        return quantity == null || buyPrice == null ? null : buyPrice / quantity;
    }
}
