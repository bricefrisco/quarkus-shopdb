package com.ecocitycraft.shopdb.database;

import com.ecocitycraft.shopdb.models.chestshops.Location;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

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
            @AttributeOverride(name="x", column=@Column(name="i_x")),
            @AttributeOverride(name="y", column=@Column(name="i_y")),
            @AttributeOverride(name="z", column=@Column(name="i_z"))
    })
    public Location iBounds;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="x", column=@Column(name="o_x")),
            @AttributeOverride(name="y", column=@Column(name="o_y")),
            @AttributeOverride(name="z", column=@Column(name="o_z"))
    })
    public Location oBounds;

    @OneToMany(mappedBy = "town", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<ChestShop> chestShops;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "region_mayors", joinColumns = @JoinColumn(name="towns_id"), inverseJoinColumns = @JoinColumn(name="mayors_id"))
    public List<Player> mayors;

    public Boolean active;

    @Column(name="last_updated")
    public Timestamp lastUpdated;

    public static Region find(Server server, String name) {
        if (server == null || name == null) return null;
        return Region.find("server = ?1 AND name = ?2", server, name.toLowerCase(Locale.ROOT)).firstResult();
    }



    public static boolean hasConflictingRegion(Location iBounds, Location oBounds, Server server) {
        // Check if any region overlaps with the inner and outer bounds
        List<Region> conflictingRegions = findByLocations(iBounds, oBounds, server);
        if (conflictingRegions != null && conflictingRegions.size() > 0) {
            return true;
        }

        // Check if this region is inside of another region
        conflictingRegions = Region.findInCoordinates(iBounds, oBounds, server);
        return conflictingRegions != null && conflictingRegions.size() > 0;
    }

    public static  List<Region> findByLocations(Location iBounds, Location oBounds, Server server) {
        List<Region> regions = findOverlapping(iBounds.getX(), iBounds.getY(), iBounds.getZ(), server);
        if (regions != null && regions.size() > 0) return regions;
        return findOverlapping(oBounds.getX(), oBounds.getY(), oBounds.getZ(), server);
    }

    public static List<Region> findOverlapping(int x, int y, int z, Server server) {
        return Region.find(
                "server = ?1 AND " +
                        "i_x <= ?2 AND o_x >= ?2 AND " +
                        "i_y <= ?3 AND o_y >= ?3 AND " +
                        "i_z <= ?4 AND o_z >= ?4",
                server, x, y, z
        ).list();
    }

    public static List<Region> findInCoordinates(Location iBounds, Location oBounds, Server server) {
        return findInCoordinates(iBounds.getX(), oBounds.getX(), iBounds.getZ(), oBounds.getZ(), server);
    }

    public static List<Region> findInCoordinates(int ix, int ox, int iz, int oz, Server server) {
        return Region.find(
                "server = ?1 AND " +
                        "i_x >= ?2 AND o_x <= ?3 AND " +
                        "i_z >= ?4 AND o_z <= ?5",
                server, ix, ox, iz, oz
        ).list();
    }
}
