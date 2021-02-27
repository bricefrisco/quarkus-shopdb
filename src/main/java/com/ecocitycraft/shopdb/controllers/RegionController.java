package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopDto;
import com.ecocitycraft.shopdb.models.chestshops.ChestShopMapper;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.ecocitycraft.shopdb.models.chestshops.TradeType;
import com.ecocitycraft.shopdb.models.players.PlayerDto;
import com.ecocitycraft.shopdb.models.players.PlayerMapper;
import com.ecocitycraft.shopdb.models.regions.RegionDto;
import com.ecocitycraft.shopdb.models.regions.RegionMapper;
import com.ecocitycraft.shopdb.models.regions.RegionRequest;
import com.ecocitycraft.shopdb.services.ChestShopBatchProcessor;
import com.ecocitycraft.shopdb.services.RegionBatchProcessor;
import com.ecocitycraft.shopdb.utils.APIKeyValidator;
import com.ecocitycraft.shopdb.utils.ExceptionMessage;
import com.ecocitycraft.shopdb.utils.Pagination;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/regions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegionController {
    Logger LOGGER = LoggerFactory.getLogger(RegionController.class);

    @Inject
    RegionBatchProcessor regionBatchProcessor;

    @Inject
    APIKeyValidator apiKeyValidator;

    @Inject
    ChestShopBatchProcessor chestShopBatchProcessor;

    @GET
    public PaginatedResponse<RegionDto> getRegions(
            @QueryParam("page") Integer page,
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("server") Server server,
            @QueryParam("active") Boolean active,
            @QueryParam("name") String name
    ) {
        LOGGER.info("GET /regions");
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 6;
        if (page < 1) throw new RuntimeException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new RuntimeException(ExceptionMessage.INVALID_PAGE_SIZE);
        if (active == null) active = Boolean.FALSE;
        if (name == null) name = "";


        PanacheQuery<Region> regions = Region.find(
            "(?1 = '' OR server = ?1) AND " +
                    "(?2 = false OR active = true) AND " +
                    "(?3 = '' OR name = ?3)",
                Server.toString(server),
                active,
                name
        );
        long totalResults = regions.count();
        List<RegionDto> results = regions.page(page - 1, pageSize).stream().map(RegionMapper.INSTANCE::toRegionDto).collect(Collectors.toList());
        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    @GET
    @Path("region-names")
    public List<PanacheEntityBase> getRegionNames(@QueryParam("server") Server server, @QueryParam("active") Boolean active) {
        LOGGER.info("GET /region-names");
        if (active == null) active = Boolean.FALSE;
        return Region.find("SELECT DISTINCT name FROM Region WHERE " +
                "(?1 = '' OR server = ?1) AND " +
                "(?2 = false OR active = true) " +
                "ORDER BY name", Server.toString(server), active).list();
    }

    @GET
    @Path("{server}/{name}")
    public RegionDto getRegion(
            @PathParam("server") Server server,
            @PathParam("name") String name
    ) {
        LOGGER.info("GET /regions/" + server + "/" + name);
        return RegionMapper.INSTANCE.toRegionDto(Region.find(server, name));
    }

    @GET
    @Path("{server}/{name}/players")
    public PaginatedResponse<PlayerDto> getRegionOwners(
            @PathParam("server") Server server,
            @PathParam("name") String name,
            @QueryParam("page") Integer page,
            @QueryParam("pageSize") Integer pageSize
    ) {
        LOGGER.info("GET /regions/" + server + "/" + name + "/players");
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 6;
        if (page < 1) throw new RuntimeException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new RuntimeException(ExceptionMessage.INVALID_PAGE_SIZE);

        Region region = Region.find(server, name);
        List<Player> players = Pagination.getPage(region.mayors, page, pageSize);
        int totalResults = players.size();
        List<PlayerDto> results = players.stream().map(PlayerMapper.INSTANCE::toPlayerDto).collect(Collectors.toList());
        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    @GET
    @Path("{server}/{name}/chest-shops")
    public PaginatedResponse<ChestShopDto> getRegionChestShops(
            @PathParam("server") Server server,
            @PathParam("name") String name,
            @QueryParam("page") Integer page,
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("tradeType") TradeType tradeType
            ) {
        LOGGER.info("GET /regions/" + server + "/" + name + "/chest-shops");
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 6;
        if (page < 1) throw new RuntimeException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new RuntimeException(ExceptionMessage.INVALID_PAGE_SIZE);
        if (tradeType == null) tradeType = TradeType.BUY;

        Region region = Region.find(server, name);
        PanacheQuery<ChestShop> chestShops = ChestShop.find("town = ?1 AND isHidden = false AND " +
                "(?2 = false OR is_buy_sign = true) AND " +
                "(?3 = false OR is_sell_sign = true)", region, tradeType == TradeType.BUY, tradeType == TradeType.SELL);

        long totalResults = chestShops.count();
        List<ChestShopDto> results = chestShops.page(page - 1, pageSize).stream().map(ChestShopMapper.INSTANCE::toChestShopDto).collect(Collectors.toList());

        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    @POST
    @Transactional
    @Consumes("application/json")
    public String processRegions(@GZIP List<RegionRequest> requests, @HeaderParam("Authorization") String authHeader) throws Exception {
        apiKeyValidator.validateAPIKey(authHeader);
        return regionBatchProcessor.processRegions(requests);
    }

    @PUT
    @Path("{server}/{name}")
    @Transactional
    public String updateRegionActive(
            @PathParam("server") Server server,
            @PathParam("name") String name,
            @QueryParam("active") Boolean active,
            @HeaderParam("Authorization") String authHeader
    ) throws Exception {
        LOGGER.info("PUT /regions/" + server + "/" + name + "/chest-shops?active=" + active);
        apiKeyValidator.validateAPIKey(authHeader);
        if (active == null) active = Boolean.FALSE;
        Region region = Region.find(server, name);
        if (region == null) throw new RuntimeException(String.format(ExceptionMessage.INVALID_REGION, name, server));
        region.active = active;
        if (active) {
            chestShopBatchProcessor.showChestShops(region);
        } else {
            chestShopBatchProcessor.hideChestShops(region);
        }
        region.persistAndFlush();
        return "Successfully updated region '" + region.name + "' on server " + Server.toString(region.server);
    }

}
