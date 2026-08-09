package me.ppgome.wayfinder.database.place;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CategoryTable {

    private Wayfinder plugin;
    private Dao<Category, String> categoryDao;

    public CategoryTable(Wayfinder plugin, Dao<Category, String> categoryDao) {
        this.plugin = plugin;
        this.categoryDao = categoryDao;
    }

    //------------------------------------------------------------------------------------------------------------------

    public CompletableFuture<List<Category>> getAllCategories() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return categoryDao.queryForAll();
            } catch(SQLException e) {
                return null;
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to load categories from database");
            e.printStackTrace();
            return null;
        });
    }

    public void delete(Category category) {
        CompletableFuture.runAsync(() -> {
            try {
                categoryDao.delete(category);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException deleting category: " + category.getName());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to delete category: " + category.getName());
            e.printStackTrace();
            return null;
        });
    }

    public void save(Category category) {
        CompletableFuture.runAsync(() -> {
            try {
                categoryDao.createOrUpdate(category);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException saving category: " + category.getName());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to save category: " + category.getName());
            e.printStackTrace();
            return null;
        });
    }

}
