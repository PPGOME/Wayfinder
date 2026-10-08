package me.ppgome.wayfinder.database;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;

public class WaypointTable {

    private final Wayfinder plugin;
    private final Dao<Waypoint, Integer> waypointDao;

    public WaypointTable(Wayfinder plugin, Dao<Waypoint, Integer> waypointDao) {
        this.plugin = plugin;
        this.waypointDao = waypointDao;
    }

}
