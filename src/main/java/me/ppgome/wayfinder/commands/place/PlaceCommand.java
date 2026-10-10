package me.ppgome.wayfinder.commands.place;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.guis.place.PlaceMainGui;
import me.ppgome.wayfinder.navigation.ParticleNavigator;
import me.ppgome.wayfinder.text.PlaceholderParser;
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
            String placeName = String.join(" ", args);
            Place place = cache.getPlace(placeName);
            if(place != null) {
                if(!cache.isRouting(player.getUniqueId())) {
                    new ParticleNavigator(Wayfinder.PLUGIN, player, place).startDisplaying();
                    player.sendMessage(PlaceholderParser.of(config.PLACE_ROUTING_TO).place(place.getName()).parse());
                } else {
                    player.sendMessage(PlaceholderParser.of(config.PLACE_ALREADY_ROUTING).place(place.getName()).parse());
                }
            } else {
                player.sendMessage(PlaceholderParser.of(config.PLACE_DOES_NOT_EXIST).place(placeName).parse());
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(Player player, String name, String[] args) {
        if(args.length == 1) {
            return cache.getAllPlaces()
                    .keySet()
                    .stream()
                    .filter(placeName -> placeName != null && placeName.toLowerCase().contains(args[0]))
                    .map(String::toLowerCase)
                    .toList();
        }
        return Collections.emptyList();
    }
}


