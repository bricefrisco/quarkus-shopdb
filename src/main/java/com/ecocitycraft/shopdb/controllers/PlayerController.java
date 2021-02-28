package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.exceptions.SDBIllegalArgumentException;
import com.ecocitycraft.shopdb.exceptions.SDBNotFoundException;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopDto;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopMapper;
import com.ecocitycraft.shopdb.models.chestshops.TradeType;
import com.ecocitycraft.shopdb.models.players.PlayerDto;
import com.ecocitycraft.shopdb.models.players.PlayerMapper;
import com.ecocitycraft.shopdb.models.regions.RegionDto;
import com.ecocitycraft.shopdb.models.regions.RegionMapper;
import com.ecocitycraft.shopdb.exceptions.ExceptionMessage;
import com.ecocitycraft.shopdb.utils.Pagination;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/players")
@Produces(MediaType.APPLICATION_JSON)
public class PlayerController {
    Logger LOGGER = LoggerFactory.getLogger(PlayerController.class);

    @GET
    public PaginatedResponse<PlayerDto> getPlayers(
            @DefaultValue("1") @QueryParam("page") Integer page,
            @DefaultValue("6") @QueryParam("pageSize") Integer pageSize,
            @DefaultValue("") @QueryParam("name") String name
    ) {
        LOGGER.info("GET /players");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);

        PanacheQuery<Player> players = Player.find(name);
        long totalResults = players.count();
        List<PlayerDto> results = players.page(page - 1, pageSize).stream().map(PlayerMapper.INSTANCE::toPlayerDto).collect(Collectors.toList());

        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    @GET
    @Path("player-names")
    public List<PanacheEntityBase> getPlayerNames() {
        return Player.findPlayerNames();
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
            @DefaultValue("1") @QueryParam("page") Integer page,
            @DefaultValue("6") @QueryParam("pageSize") Integer pageSize,
            @PathParam("name") String name
    ) {
        LOGGER.info("GET /players/" + name + "/regions");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);
        if (name == null || name.isEmpty()) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_PLAYER_NAME);

        Player p = Player.findByName(name);
        if (p == null) throw new SDBNotFoundException(String.format(ExceptionMessage.PLAYER_NOT_FOUND, name));

        List<Region> regions = p.towns;
        int totalResults = regions.size();
        regions = Pagination.getPage(p.towns, page, pageSize);
        List<RegionDto> results = regions.stream().map(RegionMapper.INSTANCE::toRegionDto).collect(Collectors.toList());

        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    @GET
    @Path("{name}/chest-shops")
    public PaginatedResponse<ChestShopDto> getPlayerChestShops(
            @DefaultValue("1") @QueryParam("page") Integer page,
            @DefaultValue("6") @QueryParam("pageSize") Integer pageSize,
            @DefaultValue("buy") @QueryParam("tradeType") TradeType tradeType,
            @PathParam("name") String name) {
        LOGGER.info("GET /players/" + name + "/chest-shops");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);
        if (name == null || name.isEmpty()) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_PLAYER_NAME);

        Player p = Player.findByName(name);
        if (p == null) throw new SDBNotFoundException(String.format(ExceptionMessage.PLAYER_NOT_FOUND, name));

        PanacheQuery<ChestShop> chestShops = ChestShop.findOwnedBy(p, tradeType);

        long totalResults = chestShops.count();
        List<ChestShopDto> results = chestShops.page(page - 1, pageSize).stream().map(ChestShopMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());
        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }
}
