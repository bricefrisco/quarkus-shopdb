package com.ecocitycraft.shopdb.models.players;

import com.ecocitycraft.shopdb.database.Player;
import com.ecocitycraft.shopdb.database.Region;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PlayerMapper {
    PlayerMapper INSTANCE = Mappers.getMapper(PlayerMapper.class);

    PlayerRegionDto toPlayerRegionDto(Region region);

    PlayerDto toPlayerDto(Player player);

    @AfterMapping
    default void mapNumChestShops(Player player, @MappingTarget PlayerDto playerDto) {
        playerDto.setNumChestShops(player.chestShops.size());
    }
}
