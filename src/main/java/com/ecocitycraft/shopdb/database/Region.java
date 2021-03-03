package com.ecocitycraft.shopdb.database;

import com.ecocitycraft.shopdb.models.chestshops.Location;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import com.ecocitycraft.shopdb.models.chestshops.SortBy;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "region")
public class Region extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public Server server;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "i_x")),
            @AttributeOverride(name = "y", column = @Column(name = "i_y")),
            @AttributeOverride(name = "z", column = @Column(name = "i_z"))
    })
    public Location iBounds;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "o_x")),
            @AttributeOverride(name = "y", column = @Column(name = "o_y")),
            @AttributeOverride(name = "z", column = @Column(name = "o_z"))
    })
    public Location oBounds;

    @OneToMany(mappedBy = "town", fetch = FetchType.LAZY)
    public List<ChestShop> chestShops;

    @ManyToMany
    @JoinTable(name = "region_mayors", joinColumns = @JoinColumn(name = "towns_id"), inverseJoinColumns = @JoinColumn(name = "mayors_id"))
    public List<Player> mayors;

    public Boolean active;

    @Column(name = "last_updated")
    public Timestamp lastUpdated;

    public static PanacheQuery<Region> find(Server server, Boolean active, String name, SortBy sortBy) {
        if (sortBy == SortBy.NUM_PLAYERS) {
            return Region.find("SELECT r FROM Region r LEFT JOIN r.mayors m " +
                    "WHERE (?1 = '' OR r.server = ?1) AND " +
                    "(?2 = false OR r.active = true) AND " +
                    "(?3 = '' OR r.name = ?3) " +
                    "GROUP BY r.id ORDER BY COUNT(m.id) DESC",
                    Server.toString(server), active, name);
        }

        if (sortBy == SortBy.NUM_CHEST_SHOPS) {
            return Region.find("SELECT r FROM Region r LEFT JOIN r.chestShops c " +
                    "WHERE (?1 = '' OR r.server = ?1) AND " +
                    "(?2 = false OR r.active = true) AND " +
                    "(?3 = '' OR r.name = ?3) " +
                    "GROUP BY r.id ORDER BY COUNT(c.id) DESC",
                    Server.toString(server), active, name);
        }

        return Region.find(
                "(?1 = '' OR server = ?1) AND " +
                        "(?2 = false OR active = true) AND " +
                        "(?3 = '' OR name = ?3)",
                Sort.by("name"),
                Server.toString(server), active, name);
    }

    public static Region find(Server server, String name) {
        if (server == null || name == null) return null;
        return Region.find("server = ?1 AND name = ?2", server, name.toLowerCase(Locale.ROOT)).firstResult();
    }

    public static List<PanacheEntityBase> findRegionNames(Server server, Boolean active) {
        return Region.find("SELECT DISTINCT name FROM Region WHERE " +
                "(?1 = '' OR server = ?1) AND " +
                "(?2 = false OR active = true) " +
                "ORDER BY name", Server.toString(server), active).list();
    }

    public static List<Region> findByCoordinates(int x, int y, int z, Server server) {
        return Region.find(
                "server = ?1 AND " +
                        "i_x <= ?2 AND o_x >= ?2 AND " +
                        "i_y <= ?3 AND o_y >= ?3 AND " +
                        "i_z <= ?4 AND o_z >= ?4",
                server, x, y, z).list();
    }

    public String getName() {
        return name.toLowerCase(Locale.ROOT);
    }

    public void setName(String name) {
        this.name = name.toLowerCase(Locale.ROOT);
    }

    public Location getiBounds() {
        if (iBounds == null) iBounds = new Location();
        return iBounds;
    }

    public Location getoBounds() {
        if (oBounds == null) oBounds = new Location();
        return oBounds;
    }
}
