package me.ppgome.wayfinder.database.place;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlaceTable {

    private final Wayfinder plugin;
    private final Dao<Place, String> placeDao;

    public PlaceTable(Wayfinder plugin, Dao<Place, String> placeDao) {
        this.plugin = plugin;
        this.placeDao = placeDao;
    }

    public CompletableFuture<List<Place>> getAllPlaces() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return placeDao.queryForAll();
            } catch(SQLException e) {
                return null;
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to load places from database");
            e.printStackTrace();
            return null;
        });
    }

    public void delete(Place place) {
        CompletableFuture.runAsync(() -> {
            try {
                placeDao.delete(place);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException deleting place: " + place.getName());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to delete place: " + place.getName());
            e.printStackTrace();
            return null;
        });
    }

    public void save(Place place) {
        CompletableFuture.runAsync(() -> {
            try {
                placeDao.createOrUpdate(place);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException saving place: " + place.getName());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to save place: " + place.getName());
            e.printStackTrace();
            return null;
        });
    }

}
