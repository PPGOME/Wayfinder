package me.ppgome.wayfinder.database;

import com.j256.ormlite.dao.Dao;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.CategorizedPlace;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerMetaTable {

    private final Wayfinder plugin;
    private final Dao<PlayerMeta, UUID> playerMetaDao;

    public PlayerMetaTable(Wayfinder plugin, Dao<PlayerMeta, UUID> playerMetaDao) {
        this.plugin = plugin;
        this.playerMetaDao = playerMetaDao;
    }

    //------------------------------------------------------------------------------------------------------------------

    public CompletableFuture<PlayerMeta> getPlayerMeta(Player player) {
        return getPlayerMetaByUUID(player.getUniqueId());
    }

    public CompletableFuture<PlayerMeta> getPlayerMetaByUUID(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return playerMetaDao.queryForId(uuid);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException fetching playermeta: " + uuid);
                e.printStackTrace();
                return null;
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to fetch playermeta: " + uuid);
            e.printStackTrace();
            return null;
        });
    }

    public void delete(PlayerMeta playerMeta) {
        CompletableFuture.runAsync(() -> {
            try {
                playerMetaDao.delete(playerMeta);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException deleting playermeta: " + playerMeta.getUuid());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to delete playermeta: " + playerMeta.getUuid());
            e.printStackTrace();
            return null;
        });
    }

    public void save(PlayerMeta playerMeta) {
        CompletableFuture.runAsync(() -> {
            try {
                playerMetaDao.createOrUpdate(playerMeta);
            } catch(SQLException e) {
                plugin.getLogger().severe("SQLException saving playermeta: " + playerMeta.getUuid());
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to save playermeta: " + playerMeta.getUuid());
            e.printStackTrace();
            return null;
        });
    }

}
