package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopDto;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopMapper;
import com.ecocitycraft.shopdb.models.players.PlayerDto;
import com.ecocitycraft.shopdb.models.players.PlayerMapper;
import com.ecocitycraft.shopdb.utils.ExceptionMessage;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
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
        return new PaginatedResponse<>(page, 0, totalResults, results);
    }

    @GET
    @Path("player-names")
    public List<PanacheEntityBase> getPlayerNames(@QueryParam("active") Boolean active) {
        if (active == null) active = false;
        return Player.find("SELECT name FROM Player WHERE (?1 = false OR active = true) ORDER BY name", active).list();
    }

    @GET
    @Path("{name}")
    public PlayerDto getPlayer(@PathParam("name") String name) {
        return PlayerMapper.INSTANCE.toPlayerDto(Player.findByName(name));
    }

    @GET
    @Path("{name}/chest-shops")
    public PaginatedResponse<ChestShopDto> getPlayerChestShops(
            @QueryParam("page") Integer page,
            @QueryParam("pageSize") Integer pageSize,
            @PathParam("name") String name
    ) {
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 6;
        if (page < 1) throw new RuntimeException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new RuntimeException(ExceptionMessage.INVALID_PAGE_SIZE);
        Player p = Player.findByName(name);
        PanacheQuery<ChestShop> chestShops = ChestShop.find("owner = ?1 AND isHidden = false", p);
        long totalResults = chestShops.count();

        List<ChestShopDto> results = chestShops.page(page - 1, pageSize).stream().map(ChestShopMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());
        return new PaginatedResponse<>(page, 0, totalResults, results);
    }
}
