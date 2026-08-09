package me.ppgome.wayfinder.database;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;

public class TeleportRelationshipTable {

    private Wayfinder plugin;
    private Dao<TeleportRelationship, Integer> teleportRelationshipDao;

    public TeleportRelationshipTable(Wayfinder plugin, Dao<TeleportRelationship, Integer> teleportRelationshipDao) {
        this.plugin = plugin;
        this.teleportRelationshipDao = teleportRelationshipDao;
    }

}
