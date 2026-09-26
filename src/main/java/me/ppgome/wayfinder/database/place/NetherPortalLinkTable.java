package me.ppgome.wayfinder.database.place;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NetherPortalLinkTable {

    private Wayfinder plugin;
    private Dao<NetherPortalLink, String> placeNetherPortalDao;

    public NetherPortalLinkTable(Wayfinder plugin, Dao<NetherPortalLink, String> placeDao) {
        this.plugin = plugin;
        this.placeNetherPortalDao = placeDao;
    }

    public CompletableFuture<List<NetherPortalLink>> getAllNetherPortalLinks() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return placeNetherPortalDao.queryForAll();
            } catch(SQLException e) {
                return null;
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to load nether portal links from database");
            e.printStackTrace();
            return null;
        });
    }

    public void delete(NetherPortalLink link) {
        CompletableFuture.runAsync(() -> {
            try {
                placeNetherPortalDao.delete(link);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException deleting nether portal link for: " + link.getPlaceName());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to delete place nether portal: " + link.getPlaceName());
            e.printStackTrace();
            return null;
        });
    }

    public void save(NetherPortalLink link) {
        CompletableFuture.runAsync(() -> {
            try {
                placeNetherPortalDao.createOrUpdate(link);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException saving place nether portal: " + link.getPlaceName());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to save place nether portal: " + link.getPlaceName());
            e.printStackTrace();
            return null;
        });
    }

}
