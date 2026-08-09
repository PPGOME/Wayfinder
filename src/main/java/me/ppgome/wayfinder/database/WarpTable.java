package me.ppgome.wayfinder.database;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;

public class WarpTable {

    private Wayfinder plugin;
    private Dao<Warp, String> warpDao;

    public WarpTable(Wayfinder plugin, Dao<Warp, String> warpDao) {
        this.plugin = plugin;
        this.warpDao = warpDao;
    }

}
