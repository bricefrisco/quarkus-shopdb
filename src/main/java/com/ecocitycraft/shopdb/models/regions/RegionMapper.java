package com.ecocitycraft.shopdb.models.regions;

import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RegionMapper {
    RegionMapper INSTANCE = Mappers.getMapper(RegionMapper.class);

    RegionPlayerDto toRegionPlayerDto(Player player);

    RegionDto toRegionDto(Region region);

    @AfterMapping
    default void mapNumChestShops(Region region, @MappingTarget RegionDto regionDto) {
        regionDto.setNumChestShops(region.chestShops.size());
    }
}
