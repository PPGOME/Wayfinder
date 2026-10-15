package me.ppgome.wayfinder.commands.place;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.guis.place.PlaceMainGui;
import me.ppgome.wayfinder.navigation.ParticleNavigator;
import me.ppgome.wayfinder.text.PlaceholderParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import static me.ppgome.wayfinder.utils.PlaceUtils.findClosestPlace;

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
        if(args.length == 0) openGui(player);
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("nearest") && args.length >= 2) findNearestPlace(player, args);
            else commandRouting(player, args);
        }
        return true;
    }

    private void openGui(Player player) {
        if(cache.isEditingGui(player.getUniqueId())) {
            cache.reopenGui(player);
            return;
        }
        new PlaceMainGui(config, cache).display(player);
    }

    private void commandRouting(Player player, String[] args) {
        String placeName = String.join(" ", args);
        Place place = cache.getPlace(placeName.toLowerCase());
        if(place != null) {
            if(!cache.isRouting(player.getUniqueId())) {
                new ParticleNavigator(Wayfinder.PLUGIN, player, place).startDisplaying();
            } else {
                player.sendMessage(PlaceholderParser.of(config.PLACE_ALREADY_ROUTING).place(place.getName()).parse());
            }
        } else {
            player.sendMessage(PlaceholderParser.of(config.PLACE_DOES_NOT_EXIST).place(placeName).parse());
        }
    }

    private void findNearestPlace(Player player, String[] args) {
        if(args.length > 1) {
            String categoryName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            Category category = cache.getCategory(categoryName);
            if(category == null) {
                player.sendMessage(Component.text("That's not a valid category.", NamedTextColor.RED));
                return;
            }

            Place closestPlace = findClosestPlace(player.getLocation(), category);

            if (closestPlace == null) {
                player.sendMessage(Component.text("Unable to locate a place nearby.", NamedTextColor.RED));
                return;
            }

            new ParticleNavigator(Wayfinder.PLUGIN, player, closestPlace).startDisplaying();
        } else {
            player.sendMessage(Component.text("Please enter a valid category name.", NamedTextColor.RED));
        }
    }

    @Override
    public List<String> tabComplete(Player player, String name, String[] args) {
        if (args == null || args.length == 0) return Collections.emptyList();

        int currentArg = args.length - 1;
        String typed = String.join(" ", args).toLowerCase(Locale.ROOT);
        List<String> suggestions = new ArrayList<>();

        if (currentArg == 0) {
            if ("nearest".startsWith(typed)) {
                suggestions.add("nearest");
            }
            suggestions.addAll(getCompletions(getPlaceNames(), typed, currentArg));
        }
        else if (currentArg == 1 && args[0].equalsIgnoreCase("nearest")) {
            String categoryTyped = args[1].toLowerCase(Locale.ROOT);
            suggestions.addAll(getCompletions(getCategoryNames(), categoryTyped, 0));
        }
        else {
            suggestions.addAll(getCompletions(getPlaceNames(), typed, currentArg));
        }

        return suggestions;
    }
    private List<String> getCompletions(Collection<String> items, String typed, int currentArg) {
        return items.stream()
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

    private Collection<String> getPlaceNames() {
        return cache.getAllPlaces().values().stream()
                .map(Place::getName)
                .toList();
    }

    private Collection<String> getCategoryNames() {
        return cache.getAllCategories().values().stream()
                .map(Category::getName)
                .toList();
    }
}


