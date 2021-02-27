package com.ecocitycraft.shopdb.database;

import com.ecocitycraft.shopdb.models.chestshops.Location;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "chest_shop_sign")
public class ChestShop extends PanacheEntityBase {
    @Id
    public String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public Server server;

    @Embedded
    public Location location;

    @Column(nullable = false)
    public String material;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Player owner;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Region town;

    public Integer quantity;
    @Column(name="quantity_available")
    public Integer quantityAvailable;
    @Column(name="buy_price")
    public Double buyPrice;
    @Column(name="sell_price")
    public Double sellPrice;
    @Column(name="buy_price_each")
    public Double buyPriceEach;
    @Column(name="sell_price_each")
    public Double sellPriceEach;

    @Column(name="is_full")
    public Boolean isFull;
    @Column(name="is_hidden")
    public Boolean isHidden;
    @Column(name="is_buy_sign")
    public Boolean isBuySign;
    @Column(name="is_sell_sign")
    public Boolean isSellSign;

    public static List<ChestShop> findInRegion(Region region) {
        return findInLocation(region.server, region.iBounds, region.oBounds);
    }

    public static List<ChestShop> findInLocation(Server server, Location iBounds, Location oBounds) {
        int lx = iBounds.getX();
        int ly = iBounds.getY();
        int lz = iBounds.getZ();
        int ux = oBounds.getX();
        int uy = oBounds.getY();
        int uz = oBounds.getZ();
        return findInLocation(server, lx, ux, ly, uy, lz, uz);
    }

    public static List<ChestShop> findInLocation(Server server, int lx, int ux, int ly, int uy, int lz, int uz) {
        return ChestShop.find(
                "server = ?1 AND " +
                        "?2 <= x AND ?3 >= x AND " +
                        "?4 <= y AND ?5 >= y AND " +
                        "?6 <= z AND ?7 >= z",
                server, lx, ux, ly, uy, lz, uz
        ).list();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChestShop chestShop = (ChestShop) o;
        return Objects.equals(material, chestShop.material) && Objects.equals(owner, chestShop.owner) && Objects.equals(town, chestShop.town) && Objects.equals(quantity, chestShop.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, owner, town, quantity);
    }
}
