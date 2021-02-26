package com.ecocitycraft.shopdb.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

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

    @Column(name="last_seen")
    public Timestamp lastSeen;
    @Column(name="last_updated")
    public Timestamp lastUpdated;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<ChestShop> chestShops;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "region_mayors", joinColumns = @JoinColumn(name="mayors_id"), inverseJoinColumns = @JoinColumn(name="towns_id"))
    public List<Region> towns;

    public Boolean active;

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
}
