package com.ecocitycraft.shopdb.models.chestshops;

import com.ecocitycraft.shopdb.database.ChestShop;
import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ChestShopMapper {
    ChestShopMapper INSTANCE = Mappers.getMapper(ChestShopMapper.class);

    ChestShopRegionDto toChestShopRegionDto(Region region);

    ChestShopPlayerDto toChestShopPlayerDto(Player player);

    ChestShopDto toChestShopDto(ChestShop chestShop);
}
