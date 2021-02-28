package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.services.BotShopProcessor;
import com.ecocitycraft.shopdb.models.bot.BotShopRequest;
import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.*;
import com.ecocitycraft.shopdb.services.ChestShopBatchProcessor;
import com.ecocitycraft.shopdb.utils.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

@Path("/chest-shops")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChestShopController {
    Logger LOGGER = LoggerFactory.getLogger(ChestShopController.class);

    @Inject
    ChestShopBatchProcessor chestShopBatchProcessor;

    @Inject
    APIKeyValidator apiKeyValidator;

    @Inject
    BotShopProcessor botShopProcessor;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<ChestShopDto> getChestShopSigns(
            @QueryParam("page") Integer page,
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("material") String material,
            @QueryParam("server") Server server,
            @QueryParam("tradeType") TradeType tradeType,
            @QueryParam("hideUnavailable") Boolean hideUnavailable,
            @QueryParam("sortBy") SortBy sortBy,
            @QueryParam("distinct") Boolean distinct)
    {
        LOGGER.info("GET /chest-shops");
        if (page == null) page = 1;
        if (page < 1) throw new BadRequestException(ExceptionMessage.INVALID_PAGE);
        if (pageSize == null) pageSize = 6;
        if (pageSize > 100 || pageSize < 1) throw new BadRequestException(ExceptionMessage.INVALID_PAGE_SIZE);
        if (tradeType == null) tradeType = TradeType.BUY;
        if (sortBy == null) sortBy = SortBy.BEST_PRICE;
        if (hideUnavailable == null) hideUnavailable = Boolean.FALSE;
        if (distinct == null) distinct = Boolean.FALSE;
        if (material == null) material = "";

        Sort sort;

        if (sortBy == SortBy.BEST_PRICE) {
            if (tradeType == TradeType.BUY) {
                sort = Sort.by("buyPriceEach").ascending();
            } else {
                sort = Sort.by("sellPriceEach").descending();
            }
        } else if (sortBy == SortBy.QUANTITY_AVAILABLE) {
            sort = Sort.by("quantityAvailable").descending();
        } else if (sortBy == SortBy.QUANTITY) {
            sort = Sort.by("quantity").descending();
        } else {
            sort = Sort.by("material").descending();
        }

        PanacheQuery<ChestShop> chestShops = ChestShop.find(
                 "(?1 = '' OR material = ?1) AND " +
                        "(?2 IS FALSE OR is_buy_sign = true) AND " +
                        "(?2 IS TRUE OR is_sell_sign = true) AND " +
                        "(?3 = '' OR server = ?3) AND " +
                        "(?4 IS FALSE OR is_full = false) AND " +
                        "(?5 IS FALSE OR quantity_available > 0) AND " +
                        "isHidden = false",
                sort,
                material,
                tradeType == TradeType.BUY,
                Server.toString(server),
                hideUnavailable && tradeType == TradeType.SELL,
                hideUnavailable && tradeType == TradeType.BUY
        );

        if (!distinct) {
            long totalResults = chestShops.count();
            List<ChestShopDto> results = chestShops.page(page - 1, pageSize).stream().map(ChestShopMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());
            return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, shuffle(results, tradeType, sortBy));
        }

        LinkedHashMap<ChestShop, ChestShop> distinctValues = new LinkedHashMap<>();
        TradeType finalTradeType = tradeType;
        chestShops.stream().forEach(cs -> {
            ChestShop cs2 = distinctValues.get(cs);
            if (
                    cs2 == null ||
                            finalTradeType == TradeType.BUY && cs.quantityAvailable > cs2.quantityAvailable ||
                            finalTradeType == TradeType.SELL && cs.quantityAvailable < cs2.quantityAvailable
            ) {
                distinctValues.put(cs, cs);
            }
        });

        long totalResults = distinctValues.keySet().size();
        List<ChestShopDto> results = Pagination.getPage(new LinkedList<>(distinctValues.keySet()), page, pageSize).stream().map(ChestShopMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());
        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, shuffle(results, tradeType, sortBy));
    }

    @GET
    @Path("material-names")
    public List<PanacheEntityBase> getChestShopSignMaterialNames(@QueryParam("server") Server server, @QueryParam("tradeType") TradeType tradeType) {
        LOGGER.info("GET /chest-shops/material-names");
        if (tradeType == null) tradeType = TradeType.BUY;
        return ChestShop.find("SELECT DISTINCT material FROM ChestShop " +
                        "WHERE (?1 = '' OR server = ?1) AND " +
                        "(?2 IS FALSE OR is_buy_sign = true) AND " +
                        "(?2 IS TRUE OR is_sell_sign = true) " +
                        "ORDER BY material",
                Server.toString(server),
                tradeType == TradeType.BUY).list();
    }

    @POST
    @Path("bot")
    @Transactional
    public String createChestShopSigns(BotShopRequest botShopRequest, @HeaderParam("Authorization") String authHeader) throws Exception {
        apiKeyValidator.validateAPIKey(authHeader);
        return botShopProcessor.processShopSigns(botShopRequest);
    }

    @POST
    @Transactional
    public String createChestShopSigns(@GZIP List<ShopEvent> shopEvents, @HeaderParam("Authorization") String authHeader) throws Exception {
        apiKeyValidator.validateAPIKey(authHeader);
        return chestShopBatchProcessor.createChestShopSigns(shopEvents);
    }

    private List<ChestShopDto> shuffle(List<ChestShopDto> dtos, TradeType tradeType, SortBy sortBy) {
        if (sortBy != SortBy.BEST_PRICE) return dtos;
        List<ChestShopDto> results = new ArrayList<>();

        HashMap<Double, List<ChestShopDto>> priceMap = new HashMap<>();

        for (ChestShopDto dto : dtos) {
            Double price = tradeType == TradeType.BUY ? dto.getBuyPriceEach() : dto.getSellPriceEach();

            List<ChestShopDto> samePrices = priceMap.get(price);
            if (samePrices == null) {
                samePrices = new ArrayList<>();
                samePrices.add(dto);
                priceMap.put(price, samePrices);
            } else {
                samePrices.add(dto);
            }
        }

        for (List<ChestShopDto> samePrices : priceMap.values()) {
            Collections.shuffle(samePrices);
            results.addAll(samePrices);
        }

        return results.stream().sorted((a, b) -> {
            if (tradeType == TradeType.BUY) {
                return Double.compare(a.getBuyPriceEach(), b.getBuyPriceEach());
            } else {
                return Double.compare(b.getSellPriceEach(), a.getSellPriceEach());
            }
        }).collect(Collectors.toList());
    }
}
