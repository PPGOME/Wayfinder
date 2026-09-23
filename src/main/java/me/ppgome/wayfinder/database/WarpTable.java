package me.ppgome.wayfinder.database;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class WarpTable {

    private Wayfinder plugin;
    private Dao<Warp, String> warpDao;

    public WarpTable(Wayfinder plugin, Dao<Warp, String> warpDao) {
        this.plugin = plugin;
        this.warpDao = warpDao;
    }

    public void delete(Warp warp) {
        CompletableFuture.runAsync(() -> {
            try {
                warpDao.delete(warp);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException deleting warp: " + warp.getName());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to delete warp: " + warp.getName());
            e.printStackTrace();
            return null;
        });
    }

    public void save(Warp warp) {
        CompletableFuture.runAsync(() -> {
            try {
                warpDao.createOrUpdate(warp);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException saving warp: " + warp.getName());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to save warp: " + warp.getName());
            e.printStackTrace();
            return null;
        });
    }

}
