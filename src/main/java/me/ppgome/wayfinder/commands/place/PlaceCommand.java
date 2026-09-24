package me.ppgome.wayfinder.commands.place;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.guis.place.PlaceMainGui;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PlaceCommand implements CommandHandler {

    Wayfinder plugin;
    Cache cache;
    WConfig config;

    public PlaceCommand(Wayfinder plugin) {
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
            new PlaceMainGui(config, cache).display(player);
        } else {

        }
        return true;
    }

    @Override
    public List<String> tabComplete(Player player, String name, String[] args) {
        if(args.length == 1) {
            return cache.getAllPlaces()
                    .keySet()
                    .stream()
                    .filter(placeName -> placeName != null && placeName.contains(args[0]))
                    .toList();
        }
        return Collections.emptyList();
    }
}


