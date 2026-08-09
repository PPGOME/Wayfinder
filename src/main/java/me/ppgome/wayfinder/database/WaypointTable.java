package me.ppgome.wayfinder.database;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;

public class WaypointTable {

    private Wayfinder plugin;
    private Dao<Waypoint, Integer> waypointDao;

    public WaypointTable(Wayfinder plugin, Dao<Waypoint, Integer> waypointDao) {
        this.plugin = plugin;
        this.waypointDao = waypointDao;
    }

}
