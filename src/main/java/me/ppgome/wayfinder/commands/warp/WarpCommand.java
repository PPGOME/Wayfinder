package me.ppgome.wayfinder.commands.warp;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.database.Warp;
import me.ppgome.wayfinder.text.PlaceholderParser;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WarpCommand implements CommandHandler {

    Wayfinder plugin;
    Cache cache;
    WConfig config;

    public WarpCommand(Wayfinder plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
        this.config = plugin.getConfiguration();
    }

    @Override
    public boolean execute(Player player, String name, String[] args) {
        if(args.length == 0) {
            if(cache.isEditingGui(player.getUniqueId())) {
                cache.reopenGui(player);
                return true;
            }
            new me.ppgome.wayfinder.guis.warp.WarpMainGui(config, cache).display(player);
        } else {
            String warpName = String.join(" ", args);
            Warp warp = cache.getWarp(warpName);
            if(warp == null || !player.hasPermission(warp.getPermission())) {
                player.sendMessage(PlaceholderParser.of(config.WARP_WARP_DOESNT_EXIST).prefix(config.PREFIX).parse());
            } else {
                player.teleport(warp.getLocation());
                player.sendMessage(PlaceholderParser.of(config.WARP_WARP_SUCCESS).prefix(config.PREFIX).parse());
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(Player player, String name, String[] args) {
        if(args.length == 1) {
            List<String> results = new ArrayList<>();
            String search = args[0];

            for(Map.Entry<String, Warp> entry : cache.getAllWarps().entrySet()) {
                String warpName = entry.getKey();

                if(warpName != null && warpName.toLowerCase().contains(search.toLowerCase())) {
                    Warp warp = entry.getValue();
                    String permission = warp.getPermission();
                    if(permission == null || permission.isEmpty() || player.hasPermission(permission)) {
                        results.add(warpName);
                    }
                }
            }
            return results;
        }
        return Collections.emptyList();
    }
}
