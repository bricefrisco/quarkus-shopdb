package com.ecocitycraft.shopdb.services;

import com.ecocitycraft.shopdb.models.bot.BotPrices;
import com.ecocitycraft.shopdb.models.bot.BotQuantity;
import com.ecocitycraft.shopdb.models.bot.BotScannedShop;
import com.ecocitycraft.shopdb.models.bot.BotShopRequest;
import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.models.chestshops.Location;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class BotShopProcessor {
    Logger LOGGER = LoggerFactory.getLogger(BotShopProcessor.class);
    Pattern QUANTITY_LINE_PATTERN = Pattern.compile("^Q ([1-9][0-9]{0,4}) : C ([0-9]{0,5})$");
    Pattern PRICE_LINE_PATTERN = Pattern.compile("^([BS])\\s?([0-9.]+)\\s?:\\s?([BS])\\s?([0-9.]+)$");
    Pattern BUY_LINE_PATTERN = Pattern.compile("^B\\s?([0-9.]+)$");
    Pattern SELL_LINE_PATTERN = Pattern.compile("^S\\s?([0-9.]+)$");

    public String processShopSigns(BotShopRequest request) {
        Server server = request.getServer();
        List<BotScannedShop> shops = request.getSigns();


        Set<String> playerNames = shops.stream().map(c -> c.getNameLine().toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        HashMap<String, Player> players = Player.getOrAddPlayers(playerNames);

        int numAdded = 0;
        int numUpdated = 0;

        List<ChestShop> results = new ArrayList<>();
        for (BotScannedShop shop : shops) {
            boolean isUpdate = false;

            BotQuantity q = determineQuantity(shop.getQuantityLine());
            if (q == null) {
                LOGGER.info("Quantity is null.");
                continue;
            }

            BotPrices p = determinePrices(q.getQuantity(), shop.getPriceLine());
            if (p == null) {
                LOGGER.info("Prices are null.");
                continue;
            }

            Player pl = players.get(shop.getNameLine().toLowerCase(Locale.ROOT));
            if (pl == null) {
                LOGGER.info("Player is null: " + shop.getNameLine().toLowerCase(Locale.ROOT));
                continue;
            }

            ChestShop c;
            Optional<ChestShop> co = ChestShop.findByIdOptional(shop.getId());
            if (co.isPresent()) {
                isUpdate = true;
                c = co.get();
            } else {
                c = new ChestShop();
            }

            c.id = shop.getId();
            c.owner = pl;

            List<Region> regions = Region.findByCoordinates(shop.getLocation().getX(), shop.getLocation().getY(), shop.getLocation().getZ(), server);
            if (regions != null && regions.size() > 0) {
                if (regions.size() > 1) {
                    LOGGER.warn("Conflicting regions.");

                    Region selectedRegion = null;
                    StringBuilder sb = new StringBuilder();
                    for (Region region : regions) {
                        sb.append(region.name).append(",");
                        if (region.active) {
                            selectedRegion = region;
                        }
                    }

                    LOGGER.warn("Regions are (server: " + server + "): " + sb.toString());

                    if (selectedRegion != null) {
                        LOGGER.warn("Selected region is (server: " + server + "): " + selectedRegion.name);
                        c.town = selectedRegion;
                    } else {
                        c.town = regions.get(0);
                    }
                } else {
                    c.town = regions.get(0);
                }
            }

            c.server = server;
            c.location = shop.getLocation();

            c.quantity = q.getQuantity();
            c.quantityAvailable = q.getCount();
            c.buyPrice = p.getBuyPrice();
            c.sellPrice = p.getSellPrice();
            c.buyPriceEach = p.getBuyPriceEach();
            c.sellPriceEach = p.getSellPriceEach();
            c.isBuySign = p.getBuyPrice() != null;
            c.isSellSign = p.getSellPrice() != null;
            c.isHidden = Boolean.FALSE;
            c.isFull = Boolean.FALSE;
            c.material = shop.getMaterialLine().toLowerCase(Locale.ROOT);

            if (isUpdate) {
                numUpdated++;
            } else {
                numAdded++;
            }

            results.add(c);
        }

        ChestShop.persist(results);

        String response = "Added " + numAdded + " shops, and updated " + numUpdated + " shops.";
        LOGGER.info(response);
        return response;
    }

    public BotQuantity determineQuantity(String quantityLine) {
        try {
            Matcher m = QUANTITY_LINE_PATTERN.matcher(quantityLine);
            if (m.find()) {
                Integer quantity = Integer.parseInt(m.group(1));
                Integer count = Integer.parseInt(m.group(2));
                return new BotQuantity(quantity, count);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BotPrices determinePrices(int quantity, String priceLine) {
        try {
            Matcher m = PRICE_LINE_PATTERN.matcher(priceLine);
            if (m.find()) {
                String buyOrSell = m.group(1);
                if (buyOrSell.equalsIgnoreCase("b")) {
                    Double buyPrice = Double.parseDouble(m.group(2));
                    Double sellPrice = Double.parseDouble(m.group(4));
                    Double buyPriceEach = buyPrice / (double) quantity;
                    Double sellPriceEach = sellPrice / (double) quantity;
                    return new BotPrices(buyPrice, sellPrice, buyPriceEach, sellPriceEach);
                } else {
                    Double sellPrice = Double.parseDouble(m.group(2));
                    Double buyPrice = Double.parseDouble(m.group(4));
                    Double buyPriceEach = buyPrice / (double) quantity;
                    Double sellPriceEach = sellPrice / (double) quantity;
                    return new BotPrices(buyPrice, sellPrice, buyPriceEach, sellPriceEach);
                }
            }

            m = BUY_LINE_PATTERN.matcher(priceLine);
            if (m.find()) {
                Double buyPrice = Double.parseDouble(m.group(1));
                Double sellPrice = null;
                Double buyPriceEach = buyPrice / (double) quantity;
                Double sellPriceEach = null;
                return new BotPrices(buyPrice, sellPrice, buyPriceEach, sellPriceEach);
            }

            m = SELL_LINE_PATTERN.matcher(priceLine);
            if (m.find()) {
                Double sellPrice = Double.parseDouble(m.group(1));
                Double buyPrice = null;
                Double buyPriceEach = null;
                Double sellPriceEach = sellPrice / (double) quantity;
                return new BotPrices(buyPrice, sellPrice, buyPriceEach, sellPriceEach);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
