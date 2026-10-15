package me.ppgome.wayfinder.database.place;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import me.ppgome.wayfinder.Wayfinder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CategorizedPlaceTable {

    /**
     * Yes, this class is inherently flawed.
     *
     * It links a place to a category using their names instead of their integer IDs, as when
     * this was being created, I made the mistake of using the names as the primary keys. I updated that
     * and managed to forget to migrate this over as well.
     *
     * This will be fixed soon.
     */

    private final Wayfinder plugin;
    private final Dao<CategorizedPlace, Integer> categorizedPlaceDao;

    public CategorizedPlaceTable(Wayfinder plugin, Dao<CategorizedPlace, Integer> categorizedPlaceDao) {
        this.plugin = plugin;
        this.categorizedPlaceDao = categorizedPlaceDao;
    }

    public CompletableFuture<List<CategorizedPlace>> getAllCategories() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return categorizedPlaceDao.queryForAll();
            } catch(SQLException e) {
                return null;
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to load categories from database");
            e.printStackTrace();
            return null;
        });
    }

    public void delete(String placeName, String categoryName) {
        CompletableFuture.runAsync(() -> {
            try {
                DeleteBuilder<CategorizedPlace, Integer> db = categorizedPlaceDao.deleteBuilder();
                db.where().eq("place", placeName).and().eq("category", categoryName);
                int removed = db.delete();
                if (removed == 0) {
                    plugin.getLogger().warning("No categorized place found for place '"
                            + placeName + "', category='" + categoryName + "'");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("SQLException deleting categorized place: place '"
                        + placeName + "', category='" + categoryName + "'");
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to delete categorized place: place '"
                    + placeName + "', category='" + categoryName + "'");
            e.printStackTrace();
            return null;
        });
    }

    public void renamePlace(String oldName, String newName) {
        CompletableFuture.runAsync(() -> {
            try {
                UpdateBuilder<CategorizedPlace, Integer> ub = categorizedPlaceDao.updateBuilder();
                ub.updateColumnValue("place", newName).where().eq("place", oldName);
                ub.update();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void save(CategorizedPlace categorizedPlace) {
        CompletableFuture.runAsync(() -> {
            try {
                categorizedPlaceDao.createOrUpdate(categorizedPlace);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException saving categorized place: " + categorizedPlace.toString());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to save categorized place: " + categorizedPlace.toString());
            e.printStackTrace();
            return null;
        });
    }

}
