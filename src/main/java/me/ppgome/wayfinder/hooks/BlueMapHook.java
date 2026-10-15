package me.ppgome.wayfinder.hooks;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.utils.TextUtils;
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

    /**
     *
     * @param map
     * @param category
     * @return
     */
    private MarkerSet getOrCreateMarkerSet(BlueMapMap map, String name) {
        return map.getMarkerSets().computeIfAbsent(name, id -> MarkerSet.builder().label(name).build());
    }

    /**
     *
     * @param place
     * @return
     */
    private String markerId(Place place) {
        return TextUtils.getBluemapId(place.getName());
    }

    /**
     *
     * @param place
     */
    public void addPlaceMarker(Place place) {
        if (api == null) return;

        Location location = place.getLocation();
        if (location == null || location.getWorld() == null) return;

        String placeIcon = place.getBluemapIcon();
        Category highestCategory = getHighestCategory(place);

        if(placeIcon == null) placeIcon = highestCategory.getBluemapIcon();

        POIMarker marker = POIMarker.builder()
                .label(place.getName())
                .position(location.getX(), location.getY(), location.getZ())
                .build();

        String finalPlaceIcon = placeIcon;
        api.getWorld(location.getWorld()).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                marker.setIcon(finalPlaceIcon, 0, 0);

                getOrCreateMarkerSet(map, highestCategory != null ?
                        TextUtils.getBluemapId(highestCategory.getName()) :
                        "solo-markers")
                        .getMarkers().put(markerId(place), marker);
            }
        });

        if(place.isNetherPortal()) {
            String finalPlaceIconPortal = placeIcon;
            api.getWorld(place.getNetherPortalLink().getPartnerLocation().getWorld()).ifPresent(world -> {
                for (BlueMapMap map : world.getMaps()) {
                    marker.setIcon(finalPlaceIconPortal, 0, 0);

                    getOrCreateMarkerSet(map, highestCategory != null ?
                            TextUtils.getBluemapId(highestCategory.getName()) :
                            "solo-markers")
                            .getMarkers().put(markerId(place), marker);
                }
            });
        }
    }

    /**
     *
     * @param place
     */
    public void removePlaceMarker(Location oldLocation, String oldPlaceName) {
        if (api == null) return;
        if (oldLocation == null || oldLocation.getWorld() == null) return;

        api.getWorld(oldLocation.getWorld()).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                for (MarkerSet set : map.getMarkerSets().values()) {
                    set.getMarkers().remove(TextUtils.getBluemapId(oldPlaceName));
                }
            }
        });
    }

    /**
     *
     * @param category
     */
    public void removeCategoryMarkerSet(Category category) {
        if (api == null) return;
        String id = TextUtils.getBluemapId(category.getName());
        api.getMaps().forEach(map -> map.getMarkerSets().remove(id));
    }

    private Category getHighestCategory(Place place) {
        if(place.getBluemapIcon() == null) return null;

        Category highest = null;
        for (Category category : place.getCategories()) {
            if(category.getBluemapIcon() == null) continue;

            if (highest == null || category.getPriority() > highest.getPriority()) {
                highest = category;
            }
        }
        return highest;
    }

}