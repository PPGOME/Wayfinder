package me.ppgome.wayfinder;

import io.papermc.paper.event.player.ChatEvent;
import me.ppgome.wayfinder.database.Home;
import me.ppgome.wayfinder.database.HomeTable;
import me.ppgome.wayfinder.database.PlayerMeta;
import me.ppgome.wayfinder.database.PlayerMetaTable;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.guis.AcceptsChatGui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class WListener implements Listener {

    private final Wayfinder plugin;
    private final Cache cache;
    private final PlayerMetaTable playerMetaTable;
    private final HomeTable homeTable;

    public WListener(Wayfinder plugin, Cache cache) {
        this.plugin = plugin;
        this.cache = cache;
        this.playerMetaTable = plugin.getDatabase().getPlayerMetaTable();
        this.homeTable = plugin.getDatabase().getHomeTable();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        PlayerMeta existingPlayerMeta = cache.getPlayerMeta(playerUuid);

        // Quickly deal with it if already loaded into memory
        if(existingPlayerMeta != null) {
            offlineTeleport(player, existingPlayerMeta);
            return;
        }

        // Fetch playermeta from database
        playerMetaTable.getPlayerMeta(player).thenCompose(loadedMeta -> {
            PlayerMeta meta = (loadedMeta != null)
                    ? loadedMeta
                    : new PlayerMeta(player.getUniqueId(), player.getName());

            if (loadedMeta == null) {
                playerMetaTable.save(meta);
            }
            return homeTable.getHomesByOwner(playerUuid).thenApply(homes -> {
                if (homes != null) {
                    for (Home home : homes) {
                        home.loadItem();
                        meta.addHome(home);
                    }
                }
                return meta;
            });
        }).thenAccept(finalMeta -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                cache.addPlayerMeta(finalMeta);
                offlineTeleport(player, finalMeta);
            });
        }).exceptionally(throwable -> {
            plugin.getLogger().severe("Error loading player data on join for " + player.getName());
            throwable.printStackTrace();
            return null;
        });
    }

    private void offlineTeleport(Player player, PlayerMeta playerMeta) {
        if(playerMeta.wasMoved()) {
            player.teleport(playerMeta.getLastLocation());
            playerMeta.setMoved(false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerMeta playerMeta = cache.getPlayerMeta(player.getUniqueId());
        playerMeta.setLastLocation(player.getLocation());
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        if(cache.isEditingGui(playerUuid) && cache.isAwaitingChat(playerUuid)) {
            WGui gui = cache.getGui(playerUuid);
            if(gui instanceof AcceptsChatGui chatGui) {
                chatGui.submitChatEntry(event.message());
                chatGui.update();
                gui.display(player);
                cache.removeAwaitingChat(playerUuid);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(cache.isRouting(uuid)) cache.removeRouting(uuid);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location location = event.getFrom();
        cache.addBack(player.getUniqueId(), location);
    }

}
