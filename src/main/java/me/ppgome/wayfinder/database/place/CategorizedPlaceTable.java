package me.ppgome.wayfinder.database.place;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import me.ppgome.wayfinder.Wayfinder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CategorizedPlaceTable {

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
                QueryBuilder<CategorizedPlace, ?> qb = categorizedPlaceDao.queryBuilder();
                qb.where().eq("place", placeName).and().eq("category", categoryName);
                CategorizedPlace match = categorizedPlaceDao.queryForFirst(qb.prepare());

                if (match == null) {
                    plugin.getLogger().warning("No categorized place found for place '"
                            + placeName + "', category='" + categoryName + "'");
                    return;
                }

                categorizedPlaceDao.delete(match);
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
