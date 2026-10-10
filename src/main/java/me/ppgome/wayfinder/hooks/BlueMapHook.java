package me.ppgome.wayfinder.hooks;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.text.TextUtils;
import org.bukkit.Location;

public class BlueMapHook {

    private final Wayfinder plugin;
    private final Cache cache;
    private BlueMapAPI api; // set in onEnable

    public BlueMapHook(Wayfinder plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCache();

        BlueMapAPI.onEnable(api -> {
            this.api = api;
            cache.getAllPlaces().values().forEach(this::addPlaceMarker);
        });

        BlueMapAPI.onDisable(api -> this.api = null);
    }

    private MarkerSet getOrCreateMarkerSet(BlueMapMap map, Category category) {
        return map.getMarkerSets().computeIfAbsent(
                TextUtils.getBluemapId(category.getName()),
                id -> MarkerSet.builder().label(category.getName()).build());
    }

    private String markerId(Place place) {
        return TextUtils.getBluemapId(place.getName());
    }

    public void addPlaceMarker(Place place) {
        if (api == null) return;

        Location location = place.getLocation();
        if (location == null || location.getWorld() == null) return;

        Category highest = null;
        for (Category category : place.getCategories()) {
            if (highest == null || category.getPriority() > highest.getPriority()) {
                highest = category;
            }
        }
        if (highest == null || highest.getBluemapIcon() == null) return;

        final Category category = highest;

        api.getWorld(location.getWorld()).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                POIMarker marker = POIMarker.builder()
                        .label(place.getName())
                        .position(location.getX(), location.getY(), location.getZ())
                        .build();
                marker.setIcon(category.getBluemapIcon(), 0, 0);

                getOrCreateMarkerSet(map, category)
                        .getMarkers().put(markerId(place), marker);
            }
        });
    }

    public void removePlaceMarker(Place place) {
        if (api == null) return;
        Location location = place.getLocation();
        if (location == null || location.getWorld() == null) return;

        api.getWorld(location.getWorld()).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                for (MarkerSet set : map.getMarkerSets().values()) {
                    set.getMarkers().remove(markerId(place));
                }
            }
        });
    }

    public void removeCategoryMarkerSet(Category category) {
        if (api == null) return;
        String id = TextUtils.getBluemapId(category.getName());
        api.getMaps().forEach(map -> map.getMarkerSets().remove(id));
    }
}