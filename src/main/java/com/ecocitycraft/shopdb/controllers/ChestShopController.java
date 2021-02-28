package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.exceptions.ExceptionMessage;
import com.ecocitycraft.shopdb.exceptions.SDBIllegalArgumentException;
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
            @DefaultValue("1") @QueryParam("page") Integer page,
            @DefaultValue("6") @QueryParam("pageSize") Integer pageSize,
            @DefaultValue("") @QueryParam("material") String material,
            @QueryParam("server") Server server,
            @DefaultValue("buy") @QueryParam("tradeType") TradeType tradeType,
            @DefaultValue("false") @QueryParam("hideUnavailable") Boolean hideUnavailable,
            @DefaultValue("best-price") @QueryParam("sortBy") SortBy sortBy,
            @DefaultValue("false") @QueryParam("distinct") Boolean distinct) {
        LOGGER.info("GET /chest-shops");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize > 100 || pageSize < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);

        Sort sort = this.mapSortBy(sortBy, tradeType);
        PanacheQuery<ChestShop> chestShops = ChestShop.find(material, tradeType, server, hideUnavailable, sort);

        if (!distinct) {
            long totalResults = chestShops.count();
            List<ChestShopDto> results = chestShops.page(page - 1, pageSize)
                    .stream().map(ChestShopMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());

            return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, shuffle(results, tradeType, sortBy));
        }

        Set<ChestShop> distinctChestShops = this.findDistinctValues(chestShops, tradeType);
        long totalResults = distinctChestShops.size();
        List<ChestShopDto> results = Pagination.getPage(new LinkedList<>(distinctChestShops), page, pageSize)
                .stream().map(ChestShopMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());

        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, shuffle(results, tradeType, sortBy));
    }

    @GET
    @Path("material-names")
    public List<PanacheEntityBase> getChestShopSignMaterialNames(
            @QueryParam("server") Server server,
            @DefaultValue("buy") @QueryParam("tradeType") TradeType tradeType) {
        LOGGER.info("GET /chest-shops/material-names");

        return ChestShop.findDistinctMaterialNames(tradeType, server);
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

    private Sort mapSortBy(SortBy sortBy, TradeType tradeType) {
        if (sortBy == SortBy.BEST_PRICE && tradeType == TradeType.BUY) return Sort.by("buyPriceEach").ascending();
        if (sortBy == SortBy.BEST_PRICE && tradeType == TradeType.SELL) return Sort.by("sellPriceEach").descending();
        if (sortBy == SortBy.QUANTITY_AVAILABLE) return Sort.by("quantityAvailable").descending();
        if (sortBy == SortBy.QUANTITY) return Sort.by("quantity").descending();
        return Sort.by("material").ascending();
    }

    private Set<ChestShop> findDistinctValues(PanacheQuery<ChestShop> chestShops, TradeType tradeType) {
        LinkedHashMap<ChestShop, ChestShop> distinctValues = new LinkedHashMap<>();

        chestShops.stream().forEach(cs -> {
            ChestShop cs2 = distinctValues.get(cs);
            if (
                    cs2 == null ||
                            tradeType == TradeType.BUY && cs.quantityAvailable > cs2.quantityAvailable ||
                            tradeType == TradeType.SELL && cs.quantityAvailable < cs2.quantityAvailable
            ) {
                distinctValues.put(cs, cs);
            }
        });

        return distinctValues.keySet();
    }
}
