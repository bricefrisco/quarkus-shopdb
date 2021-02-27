package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopDto;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopMapper;
import com.ecocitycraft.shopdb.models.chestshops.TradeType;
import com.ecocitycraft.shopdb.models.players.PlayerDto;
import com.ecocitycraft.shopdb.models.players.PlayerMapper;
import com.ecocitycraft.shopdb.models.regions.RegionDto;
import com.ecocitycraft.shopdb.models.regions.RegionMapper;
import com.ecocitycraft.shopdb.utils.ExceptionMessage;
import com.ecocitycraft.shopdb.utils.Pagination;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import org.jboss.resteasy.annotations.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Path("/players")
@Produces(MediaType.APPLICATION_JSON)
public class PlayerController {
    Logger LOGGER = LoggerFactory.getLogger(PlayerController.class);

    @GET
    public PaginatedResponse<PlayerDto> getPlayers(
            @QueryParam("page") Integer page,
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("name") String name,
            @QueryParam("active") Boolean active
    ) {
        LOGGER.info("GET /players");
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 6;
        if (name == null) {
            name = "";
        } else {
            name = name.toLowerCase(Locale.ROOT);
        }
        if (active == null) active = Boolean.FALSE;
        if (page < 1) throw new RuntimeException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize >= 100) throw new RuntimeException(ExceptionMessage.INVALID_PAGE_SIZE);

        PanacheQuery<Player> players = Player.find(
                "(?1 = '' OR name = ?1) AND " +
                "(?2 = false OR active = true)",
            Sort.by("name"),
                name,
                active
        );

        long totalResults = players.count();
        List<PlayerDto> results = players.page(page - 1, pageSize).stream().map(PlayerMapper.INSTANCE::toPlayerDto).collect(Collectors.toList());
        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    @GET
    @Path("player-names")
    public List<PanacheEntityBase> getPlayerNames(@QueryParam("active") Boolean active) {
        LOGGER.info("GET /players/player-names");
        if (active == null) active = false;
        return Player.find("SELECT name FROM Player WHERE (?1 = false OR active = true) ORDER BY name", active).list();
    }

    @GET
    @Path("{name}")
    public PlayerDto getPlayer(@PathParam("name") String name) {
        LOGGER.info("GET /players/" + name);
        return PlayerMapper.INSTANCE.toPlayerDto(Player.findByName(name));
    }

    @GET
    @Path("{name}/regions")
    public PaginatedResponse<RegionDto> getPlayerRegions(
            @QueryParam("page") Integer page,
            @QueryParam("pageSize") Integer pageSize,
            @PathParam("name") String name
    ) {
        LOGGER.info("GET /players/" + name + "/regions");
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 6;
        if (page < 1) throw new RuntimeException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new RuntimeException(ExceptionMessage.INVALID_PAGE_SIZE);
        Player p = Player.findByName(name);
        List<Region> regions = p.towns;
        int totalResults = regions.size();
        regions = Pagination.getPage(p.towns, page, pageSize);
        List<RegionDto> results = regions.stream().map(RegionMapper.INSTANCE::toRegionDto).collect(Collectors.toList());
        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    @GET
    @Path("{name}/chest-shops")
    public PaginatedResponse<ChestShopDto> getPlayerChestShops(
            @QueryParam("page") Integer page,
            @QueryParam("pageSize") Integer pageSize,
            @PathParam("name") String name,
            @QueryParam("tradeType") TradeType tradeType
    ) {
        LOGGER.info("GET /players/" + name + "/chest-shops");
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 6;
        if (page < 1) throw new RuntimeException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new RuntimeException(ExceptionMessage.INVALID_PAGE_SIZE);
        if (tradeType == null) tradeType = TradeType.BUY;
        Player p = Player.findByName(name);
        PanacheQuery<ChestShop> chestShops = ChestShop.find("owner = ?1 AND isHidden = false AND " +
                "(?2 = false OR is_buy_sign = true) AND " +
                "(?3 = false OR is_sell_sign = true)", p, tradeType == TradeType.BUY, tradeType == TradeType.SELL);

        long totalResults = chestShops.count();

        List<ChestShopDto> results = chestShops.page(page - 1, pageSize).stream().map(ChestShopMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());
        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }
}
