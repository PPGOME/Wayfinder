package me.ppgome.wayfinder.database;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HomeTable {

    private Wayfinder plugin;
    private Dao<Home, Integer> homeDao;

    public HomeTable(Wayfinder plugin, Dao<Home, Integer> homeDao) {
        this.plugin = plugin;
        this.homeDao = homeDao;
    }

    public CompletableFuture<List<Home>> getAllHomes() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return homeDao.queryForAll();
            } catch(SQLException e) {
                return null;
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to load homes from database");
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<List<Home>> getHomesByOwner(UUID ownerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                homeDao.queryForAll().forEach(h -> System.out.println("Stored Home: " + h.getName() + " | Owner: " + h.getOwnerUuid()));
                return homeDao.queryForEq("ownerUuid", ownerUuid);
            } catch(SQLException e) {
                plugin.getLogger().severe("Failed to load homes for owner " + ownerUuid + " from database");
                e.printStackTrace();
                return null;
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to load homes for owner " + ownerUuid + " from database");
            e.printStackTrace();
            return null;
        });
    }

    public void delete(Home home) {
        CompletableFuture.runAsync(() -> {
            try {
                homeDao.delete(home);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException deleting home: " + home.getName());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to delete home: " + home.getName());
            e.printStackTrace();
            return null;
        });
    }

    public void save(Home home) {
        CompletableFuture.runAsync(() -> {
            try {
                homeDao.createOrUpdate(home);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException saving home: " + home.getName());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to save home: " + home.getName());
            e.printStackTrace();
            return null;
        });
    }

}
