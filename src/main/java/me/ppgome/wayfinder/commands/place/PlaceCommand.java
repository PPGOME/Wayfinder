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

import java.util.*;

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
        if (args.length == 0) return Collections.emptyList();

        String typed = String.join(" ", args).toLowerCase(Locale.ROOT);
        int currentArg = args.length - 1;

        return cache.getAllPlaces()
                .values()
                .stream()
                .map(Place::getName)
                .filter(n -> n != null && n.toLowerCase(Locale.ROOT).startsWith(typed))
                .map(n -> {
                    String[] words = n.split(" ");
                    if (currentArg >= words.length) return null;
                    return String.join(" ", Arrays.copyOfRange(words, currentArg, words.length));
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}


