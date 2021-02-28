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
    private static final String LOCATION = "%d|%d|%d|%s";
    Pattern QUANTITY_LINE_PATTERN = Pattern.compile("^Q ([1-9][0-9]{0,4}) : C ([0-9]{0,5})$");
    Pattern PRICE_LINE_PATTERN = Pattern.compile("^([BS])\\s?([0-9.]+)\\s?:\\s?([BS])\\s?([0-9.]+)$");
    Pattern BUY_LINE_PATTERN = Pattern.compile("^B\\s?([0-9.]+)$");
    Pattern SELL_LINE_PATTERN = Pattern.compile("^S\\s?([0-9.]+)$");

    public String processShopSigns(BotShopRequest request) {
        Server server = request.getServer();
        String regionName = request.getRegionName().toLowerCase(Locale.ROOT);
        Region r = Region.find(request.getServer(), regionName);
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
            c.town = r;
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

    public static String generateID(Location location, Server server) {
        String s = null;
        System.out.println("Server: " + server);
        if (server == Server.MAIN) s = "rising";
        if (server == Server.MAIN_EAST) s = "rising_n";
        if (server == Server.MAIN_NORTH) s = "rising_e";

        if (s == null) return null;
        return generateID(location.getX(), location.getY(), location.getZ(), s);
    }

    public static String generateID(int x, int y, int z, String s) {
        x = con(x);
        y = con(y);
        z = con(z);
        System.out.println("X: " + x);
        System.out.println("Y: " + y);
        System.out.println("Z: " + z);
        String location = String.format(LOCATION, x, y, z, s);
        return UUID.nameUUIDFromBytes(location.getBytes()).toString();
    }

    public static int con(int num) {
        return locToBlock(num);
    }

    public static int locToBlock(double loc) {
        return floor(loc);
    }

    public static int floor(double num) {
        int floor = (int)num;
        return (double)floor == num ? floor : floor - (int)(Double.doubleToRawLongBits(num) >>> 63);
    }
}
