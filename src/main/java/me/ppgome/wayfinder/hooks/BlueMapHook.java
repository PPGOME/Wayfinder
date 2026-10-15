package me.ppgome.wayfinder.hooks;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.NetherPortalLink;
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

        plugin.getLogger().info("Adding place marker for " + place.getName());

        Location location = place.getLocation();
        if (location == null || location.getWorld() == null) return;

        String placeIcon = place.getBluemapIcon();
        Category highestCategory = getHighestCategory(place);

        plugin.getLogger().info("Icon: " + placeIcon);
        String tempName = highestCategory != null ? highestCategory.getName() : "null";
        plugin.getLogger().info("Highest category: " + tempName);

        if(placeIcon == null) {
            if(highestCategory != null) {
                placeIcon = highestCategory.getBluemapIcon();
            } else {
                return;
            }
        }

        plugin.getLogger().info("Made it here");

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

        NetherPortalLink link = place.getNetherPortalLink();
        if (place.isNetherPortal() && link != null) {
            Location partner = link.getPartnerLocation();
            if (partner != null && partner.getWorld() != null) {
                POIMarker portalMarker = POIMarker.builder()
                        .label(place.getName())
                        .position(partner.getX(), partner.getY(), partner.getZ())
                        .build();

                api.getWorld(partner.getWorld()).ifPresent(world -> {
                    for (BlueMapMap map : world.getMaps()) {
                        portalMarker.setIcon(finalPlaceIcon, 0, 0);
                        getOrCreateMarkerSet(map, highestCategory != null ?
                                TextUtils.getBluemapId(highestCategory.getName()) :
                                "solo-markers")
                                .getMarkers().put(markerId(place), portalMarker);
                    }
                });
            }
        }
    }

    /**
     *
     * @param place
     */
    public void removePlaceMarker(Location oldLocation, String oldPlaceName) {
        if (api == null) return;
        plugin.getLogger().info("Removing place marker for " + oldPlaceName);

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