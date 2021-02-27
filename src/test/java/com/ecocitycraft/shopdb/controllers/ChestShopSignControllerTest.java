//package com.ecocitycraft.shopdb.controllers;
//
//import io.quarkus.test.junit.QuarkusTest;
//import org.junit.jupiter.api.Test;
//
//import static io.restassured.RestAssured.given;
//
//@QuarkusTest
//public class ChestShopSignControllerTest {
//
//    @Test
//    public void testGetChestShopSigns() {
//        given().when().get("/chest-shops").then().statusCode(200);
//    }
//
//    @Test
//    public void testGetChestShopSigns_pagePageSize() {
//        given().when().get("/chest-shops?page=1&pageSize=10").then().statusCode(200);
//        given().when().get("/chest-shops?page=-1").then().statusCode(400);
//        given().when().get("/chest-shops?pageSize=101").then().statusCode(400);
//        given().when().get("/chest-shops?pageSize=-1").then().statusCode(400);
//    }
//
//    @Test
//    public void testGetChestShopSigns_enumParams() {
//        given().when().get("/chest-shops?sortBy=best-price").then().statusCode(200);
//        given().when().get("/chest-shops?server=main").then().statusCode(200);
//        given().when().get("/chest-shops?tradeType=buy").then().statusCode(200);
//        given().when().get("/chest-shops?sortBy=invalid").then().statusCode(400);
//        given().when().get("/chest-shops?server=invalid").then().statusCode(400);
//        given().when().get("/chest-shops?tradeType=invalid").then().statusCode(400);
//    }
//}
