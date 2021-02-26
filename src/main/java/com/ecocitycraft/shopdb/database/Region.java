package com.ecocitycraft.shopdb.database;

import com.ecocitycraft.shopdb.models.chestshops.Location;
import com.ecocitycraft.shopdb.models.chestshops.Server;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

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
        return Region.find("server = ?1 AND name = ?2", server, name).firstResult();
    }

    public static List<Region> findByCoordinates(int x, int y, int z, Server server) {
        return Region.find(
                "server = ?1 AND " +
                        "i_x <= ?2 AND o_x >= ?2 AND " +
                        "i_y <= ?3 AND o_y >= ?3 AND " +
                        "i_z <= ?4 AND o_z >= ?4",
                server, x, y, z
        ).list();
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
