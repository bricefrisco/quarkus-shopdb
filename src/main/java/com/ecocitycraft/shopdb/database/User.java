package com.ecocitycraft.shopdb.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {
    public String username;
    public String password;
}
