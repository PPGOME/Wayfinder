package me.ppgome.wayfinder.commands.teleport;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.database.PlayerMeta;
import me.ppgome.wayfinder.database.PlayerMetaTable;
import me.ppgome.wayfinder.text.PlaceholderParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class TPHereCommand implements CommandHandler {

    Wayfinder plugin;
    Cache cache;
    WConfig config;
    PlayerMetaTable metaTable;

    public TPHereCommand(Wayfinder plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
        this.config = plugin.getConfiguration();
        this.metaTable = plugin.getDatabase().getPlayerMetaTable();
    }

    @Override
    public boolean execute(Player player, String name, String[] args) {
        if(args.length != 1) {
            player.sendMessage(PlaceholderParser.of("<red>Usage: /tphere <playername></red>").prefix(config.PREFIX).parse());
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if(targetPlayer != null) {
            targetPlayer.teleport(player.getLocation());
            player.sendMessage(PlaceholderParser.of("<green>You've teleported <yellow>" + targetPlayer.getName() + "</yellow> to you.").parse());
            targetPlayer.sendMessage(PlaceholderParser.of("<green>You've been teleported to</green> <yellow>" + player.getName()).parse());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                if(offlinePlayer != null) {
                    PlayerMeta playerMeta = cache.getPlayerMeta(offlinePlayer.getUniqueId());
                    if(playerMeta != null) {
                        playerMeta.setLastLocation(player.getLocation());
                        playerMeta.setMoved(true);
                    } else {
                        metaTable.getPlayerMetaByUUID(offlinePlayer.getUniqueId()).thenAccept(offlinePlayerMeta -> {
                            if(offlinePlayerMeta != null) {
                                offlinePlayerMeta.setLastLocation(player.getLocation());
                                offlinePlayerMeta.setMoved(true);
                                metaTable.save(offlinePlayerMeta);
                            }
                        });
                    }
                }
            });
            player.sendMessage(PlaceholderParser.of("<green>We'll try to teleport that player to this location when they log in next.").prefix(config.PREFIX).parse());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(Player player, String name, String[] args) {
        return null;
    }

}
