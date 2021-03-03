package com.ecocitycraft.shopdb.controllers;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import com.ecocitycraft.shopdb.exceptions.SDBIllegalArgumentException;
import com.ecocitycraft.shopdb.exceptions.SDBNotFoundException;
import com.ecocitycraft.shopdb.models.PaginatedResponse;
import com.ecocitycraft.shopdb.models.chestshops.*;
import com.ecocitycraft.shopdb.models.players.PlayerDto;
import com.ecocitycraft.shopdb.models.players.PlayerMapper;
import com.ecocitycraft.shopdb.models.regions.RegionDto;
import com.ecocitycraft.shopdb.models.regions.RegionMapper;
import com.ecocitycraft.shopdb.models.regions.RegionRequest;
import com.ecocitycraft.shopdb.services.ChestShopBatchProcessor;
import com.ecocitycraft.shopdb.services.RegionBatchProcessor;
import com.ecocitycraft.shopdb.utils.APIKeyValidator;
import com.ecocitycraft.shopdb.exceptions.ExceptionMessage;
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
            @DefaultValue("1") @QueryParam("page") Integer page,
            @DefaultValue("6") @QueryParam("pageSize") Integer pageSize,
            @QueryParam("server") Server server,
            @DefaultValue("false") @QueryParam("active") Boolean active,
            @DefaultValue("") @QueryParam("name") String name,
            @DefaultValue("name") @QueryParam("sortBy") SortBy sortBy
    ) {
        LOGGER.info("GET /regions");
        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);

        PanacheQuery<Region> regions = Region.find(server, active, name, sortBy);
        long totalResults = Region.find(server, active, name, SortBy.NAME).count();
        List<RegionDto> results = regions.page(page - 1, pageSize).stream().map(RegionMapper.INSTANCE::toRegionDto).collect(Collectors.toList());

        return new PaginatedResponse<>(page, Pagination.getNumPages(pageSize, totalResults), totalResults, results);
    }

    @GET
    @Path("region-names")
    public List<PanacheEntityBase> getRegionNames(
            @QueryParam("server") Server server,
            @DefaultValue("false") @QueryParam("active") Boolean active) {
        LOGGER.info("GET /region-names");
        return Region.findRegionNames(server, active);
    }

    @GET
    @Path("{server}/{name}")
    public RegionDto getRegion(
            @PathParam("server") Server server,
            @PathParam("name") String name
    ) {
        LOGGER.info("GET /regions/" + server + "/" + name);

        if (name == null) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_REGION_NAME);
        if (server == null) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_SERVER_NAME);

        Region region = Region.find(server, name);
        if (region == null) throw new SDBNotFoundException(String.format(ExceptionMessage.REGION_NOT_FOUND, name, server));

        return RegionMapper.INSTANCE.toRegionDto(Region.find(server, name));
    }

    @GET
    @Path("{server}/{name}/players")
    public PaginatedResponse<PlayerDto> getRegionOwners(
            @PathParam("server") Server server,
            @PathParam("name") String name,
            @DefaultValue("1") @QueryParam("page") Integer page,
            @DefaultValue("6") @QueryParam("pageSize") Integer pageSize) {
        LOGGER.info("GET /regions/" + server + "/" + name + "/players");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);
        if (name == null) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_REGION_NAME);
        if (server == null) throw new SDBIllegalArgumentException(ExceptionMessage.EMPTY_SERVER_NAME);

        Region region = Region.find(server, name);
        if (region == null) throw new SDBNotFoundException(String.format(ExceptionMessage.REGION_NOT_FOUND, name, server));

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
            @DefaultValue("1") @QueryParam("page") Integer page,
            @DefaultValue("6") @QueryParam("pageSize") Integer pageSize,
            @DefaultValue("buy") @QueryParam("tradeType") TradeType tradeType) {
        LOGGER.info("GET /regions/" + server + "/" + name + "/chest-shops");

        if (page < 1) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE);
        if (pageSize < 1 || pageSize > 100) throw new SDBIllegalArgumentException(ExceptionMessage.INVALID_PAGE_SIZE);

        Region region = Region.find(server, name);
        if (region == null) throw new SDBNotFoundException(String.format(ExceptionMessage.REGION_NOT_FOUND, name, server));

        PanacheQuery<ChestShop> chestShops = ChestShop.findInRegion(region, tradeType);
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
            @DefaultValue("false") @QueryParam("active") Boolean active,
            @HeaderParam("Authorization") String authHeader
    ) throws Exception {
        LOGGER.info("PUT /regions/" + server + "/" + name + "/chest-shops?active=" + active);

        apiKeyValidator.validateAPIKey(authHeader);
        Region region = Region.find(server, name);

        if (region == null) throw new SDBNotFoundException(String.format(ExceptionMessage.REGION_NOT_FOUND, name, server));
        region.active = active;

        if (active) {
            chestShopBatchProcessor.linkAndShowChestShops(region);
        } else {
            chestShopBatchProcessor.linkAndHideChestShops(region);
        }

        region.persistAndFlush();
        return "Successfully updated region '" + region.name + "' on server " + Server.toString(region.server);
    }

    @PUT
    @Path("/link-shops")
    @Transactional
    public String linkActiveRegionChestShops(@HeaderParam("Authorization") String authHeader) throws Exception {
        apiKeyValidator.validateAPIKey(authHeader);

        List<Region> regions = Region.find("active", Boolean.TRUE).list();
        for (Region region : regions) {
            chestShopBatchProcessor.linkAndShowChestShops(region);
        }

        return "Successfully linked active regions.";
    }

}
