package me.ppgome.wayfinder.database;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;

public class TeleportRelationshipTable {

    private final Wayfinder plugin;
    private final Dao<TeleportRelationship, Integer> teleportRelationshipDao;

    public TeleportRelationshipTable(Wayfinder plugin, Dao<TeleportRelationship, Integer> teleportRelationshipDao) {
        this.plugin = plugin;
        this.teleportRelationshipDao = teleportRelationshipDao;
    }

}
