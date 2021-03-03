package com.ecocitycraft.shopdb.database;

import com.ecocitycraft.shopdb.models.chestshops.SortBy;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.*;

@Entity
@Table(name = "player")
public class Player extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(unique = true, nullable = false)
    @Size(min = 3, max = 16)
    public String name;

    @Column(name = "last_seen")
    public Timestamp lastSeen;
    @Column(name = "last_updated")
    public Timestamp lastUpdated;

    @OneToMany(mappedBy = "owner")
    public List<ChestShop> chestShops;

    @ManyToMany
    @JoinTable(name = "region_mayors", joinColumns = @JoinColumn(name = "mayors_id"), inverseJoinColumns = @JoinColumn(name = "towns_id"))
    public List<Region> towns;

    public Boolean active;

    public static PanacheQuery<Player> find(String name, SortBy sortBy) {
        name = name.toLowerCase(Locale.ROOT);

        if (sortBy == SortBy.NUM_CHEST_SHOPS) {
            return Player.find("SELECT p FROM Player p LEFT JOIN p.chestShops c " +
                    "WHERE (?1 = '' OR name = ?1) " +
                    "GROUP BY p.id ORDER BY COUNT(c.id) DESC", name);
        }

        if (sortBy == SortBy.NUM_REGIONS) {
            System.out.println("sorting by # regions");
            return Player.find("SELECT p FROM Player p LEFT JOIN p.towns t " +
                    "WHERE (?1 = '' OR p.name = ?1) " +
                    "GROUP BY p.id ORDER BY COUNT(t.id) DESC", name);
        }

        return Player.find("(?1 = '' OR name = ?1)",
                Sort.by("name"),
                name
        );
    }

    public static Player findByName(String name) {
        if (name == null) return null;
        name = name.toLowerCase(Locale.ROOT);

        Optional<Player> maybePlayer = Player.find("name = ?1", name).firstResultOptional();
        return maybePlayer.orElse(null);
    }

    public static HashMap<String, Player> getOrAddPlayers(Set<String> playerNames) {
        HashMap<String, Player> players = new HashMap<>();

        for (String name : playerNames) {
            Player player = findByName(name);
            if (player == null) {
                player = new Player();
                player.name = name.toLowerCase(Locale.ROOT);
                Player.persist(player);
            }
            players.put(name, player);
        }

        return players;
    }

    public static List<PanacheEntityBase> findPlayerNames() {
        return Player.find("SELECT name FROM Player WHERE (?1 = true) ORDER BY name", true).list();
    }

    public String getName() {
        return name.toLowerCase(Locale.ROOT);
    }

    public void setName(String name) {
        this.name = name.toLowerCase(Locale.ROOT);
    }
}
