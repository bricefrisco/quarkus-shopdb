package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopDto;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.ecocitycraft.shopdb.models.chestshops.SortBy;
import com.ecocitycraft.shopdb.models.chestshops.TradeType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import javax.inject.Inject;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChestShopSignControllerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String CHEST_SHOP_ID = "abc";

    @Inject
    ChestShopController chestShopController;

    @Test
    @Order(1)
    @Transactional
    public void chest_shops_shown() {
        Player player = new Player();
        player.name = "kozz";

        Region region = new Region();
        region.name = "browntown";
        region.server = Server.MAIN;
        region.iBounds.setX(0);
        region.iBounds.setY(0);
        region.iBounds.setZ(0);
        region.oBounds.setX(200);
        region.oBounds.setY(200);
        region.oBounds.setZ(200);
        region.active = Boolean.TRUE;


        ChestShop chestShop = new ChestShop();
        chestShop.id = CHEST_SHOP_ID;
        chestShop.server = Server.MAIN;
        chestShop.location.setX(1);
        chestShop.location.setY(1);
        chestShop.location.setZ(1);
        chestShop.material = "test";
        chestShop.isHidden = Boolean.FALSE;
        chestShop.buyPrice = 1.0;
        chestShop.buyPriceEach = 1.0;
        chestShop.sellPrice = 1.0;
        chestShop.sellPriceEach = 1.0;
        chestShop.quantity = 0;
        chestShop.isBuySign = Boolean.TRUE;
        chestShop.isSellSign = Boolean.TRUE;
        chestShop.owner = player;
        chestShop.town = region;

        Player.persist(player);
        Region.persist(region);
        ChestShop.persist(chestShop);

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );


        assertEquals(chestShops.getTotalElements(), 1);
        assertEquals(chestShops.getPage(), 1);
        assertEquals(chestShops.getTotalPages(), 1);
        assertEquals(chestShops.getResults().get(0).getMaterial(), "test");
    }

    @Test
    @Order(2)
    @Transactional
    public void hidden_chest_shops_not_shown() {
        ChestShop chestShop = ChestShop.findById(CHEST_SHOP_ID);
        chestShop.isHidden = Boolean.TRUE;
        ChestShop.persist(chestShop);

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(0, chestShops.getResults().size());
        assertEquals(0, chestShops.getTotalElements());
    }

    @Test
    @Order(3)
    @Transactional
    public void test_trade_type_buy_doesnt_show_items_purchased() {
        ChestShop chestShop = ChestShop.findById(CHEST_SHOP_ID);
        chestShop.isHidden = Boolean.FALSE;
        chestShop.isBuySign = Boolean.FALSE;
        ChestShop.persist(chestShop);

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(0, chestShops.getResults().size());
        assertEquals(0, chestShops.getTotalElements());
    }

    @Test
    @Order(4)
    @Transactional
    public void test_trade_type_sell_doesnt_show_items_sold() {
        ChestShop chestShop = ChestShop.findById(CHEST_SHOP_ID);
        chestShop.isBuySign = Boolean.TRUE;
        chestShop.isSellSign = Boolean.FALSE;

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN, TradeType.SELL, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(0, chestShops.getTotalElements());
    }

    @Test
    @Order(5)
    @Transactional
    public void test_server_filter() {
        ChestShop chestShop = ChestShop.findById(CHEST_SHOP_ID);
        chestShop.server = Server.MAIN_NORTH;

        ChestShop.persist(chestShop);

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(0, chestShops.getTotalElements());

        chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test", Server.MAIN_NORTH, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(1, chestShops.getTotalElements());
    }

    @Test
    @Order(6)
    @Transactional
    public void test_item_filter() {
        ChestShop chestShop = ChestShop.findById(CHEST_SHOP_ID);
        chestShop.server = Server.MAIN;

        PaginatedResponse<ChestShopDto> chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(1, chestShops.getTotalElements());

        chestShops =
                chestShopController.getChestShopSigns(
                        1, 6, "test2", Server.MAIN, TradeType.BUY, Boolean.FALSE, SortBy.BEST_PRICE, Boolean.TRUE
                );

        assertEquals(0, chestShops.getTotalElements());
    }

    @Test
    public void shouldnt_show_hidden_signs() {
    }

    @Test
    public void testGetChestShopSigns() {
        given().when().get("/chest-shops").then().statusCode(200);
    }

    @Test
    public void testGetChestShopSigns_pagePageSize() {
        given().when().get("/chest-shops?page=1&pageSize=10").then().statusCode(200);
        given().when().get("/chest-shops?page=-1").then().statusCode(400);
        given().when().get("/chest-shops?pageSize=101").then().statusCode(400);
        given().when().get("/chest-shops?pageSize=-1").then().statusCode(400);
    }

    @Test
    public void testGetChestShopSigns_enumParams() {
        given().when().get("/chest-shops?sortBy=best-price").then().statusCode(200);
        given().when().get("/chest-shops?server=main").then().statusCode(200);
        given().when().get("/chest-shops?tradeType=buy").then().statusCode(200);
        given().when().get("/chest-shops?sortBy=invalid").then().statusCode(400);
        given().when().get("/chest-shops?server=invalid").then().statusCode(400);
        given().when().get("/chest-shops?tradeType=invalid").then().statusCode(400);
    }
}
