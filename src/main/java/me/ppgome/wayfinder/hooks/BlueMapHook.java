package me.ppgome.wayfinder.hooks;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.text.TextUtils;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Optional;

public class BlueMapHook {

    Wayfinder plugin;
    Cache cache;
    Optional<BlueMapAPI> blueMapAPI = BlueMapAPI.getInstance();

    HashMap<String, MarkerSet> marketSetMap  = new HashMap<>();
    HashMap<String, Marker> markerMap  = new HashMap<>();

    public BlueMapHook(Wayfinder plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCache();

        BlueMapAPI.onEnable(api -> {
            cache.getAllCategories().entrySet().stream().forEach(entry -> {
                addCategoryMarkerSet(entry.getValue());
            });

            cache.getAllPlaces().entrySet().stream().forEach(entry -> {
                addPlaceMarker(entry.getValue());
            });
        });
    }

    //------------------------------------------------------------------------------------------------------------------

    public void addCategoryMarkerSet(Category category) {
        if(category.getBluemapIcon() == null) return;
        Place firstPlace = category.getPlaces().stream().findFirst().orElse(null);
        if(firstPlace != null) {
            blueMapAPI.get().getWorld(category.getPlaces()).ifPresent(world -> {
                MarkerSet markerSet = MarkerSet.builder()
                        .label(category.getName())
                        .build();
                for(BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().put(TextUtils.getBluemapId(category.getName()), markerSet);
                }
            });
        } else {
            System.out.println(category.getName() + " has no markers");
        }
    }

    public void removeCategoryMarkerSet(Category category) {

    }

    public void addPlaceMarker(Place place) {
        Location location = place.getLocation();
        Category highestCategory = null;

        for(Category category : place.getCategories()) {
            if(highestCategory == null || category.getPriority() > highestCategory.getPriority()) {
                highestCategory = category;
            }
        }

        if(highestCategory == null || highestCategory.getBluemapIcon() == null) return;

        POIMarker marker = POIMarker.builder()
                .label(place.getName())
                .position(location.getX(), location.getY(), location.getZ())
                .maxDistance(1000)
                .build();
        marker.setIcon(highestCategory.getBluemapIcon(), 0, 0);
        markerMap.put(place.getName(), marker);
        MarkerSet markerSet = marketSetMap.get(highestCategory.getName());
        markerSet.getMarkers().put(TextUtils.getBluemapId(place.getName()), marker);
    }

    public void removePlaceMarker(Place place) {

    }

}
