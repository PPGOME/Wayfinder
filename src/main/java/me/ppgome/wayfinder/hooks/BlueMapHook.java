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
import org.bukkit.World;

import java.util.HashMap;
import java.util.Optional;

public class BlueMapHook {

    Wayfinder plugin;
    Cache cache;
    Optional<BlueMapAPI> blueMapAPI = BlueMapAPI.getInstance();

    HashMap<String, MarkerSet> markerSetMap = new HashMap<>();
    HashMap<String, Marker> markerMap  = new HashMap<>();

    public BlueMapHook(Wayfinder plugin) {
        System.out.println("Bluemap 1");
        this.plugin = plugin;
        this.cache = plugin.getCache();

        BlueMapAPI.onEnable(api -> {
            System.out.println("Bluemap 2");
            cache.getAllCategories().entrySet().stream().forEach(entry -> {
                addCategoryMarkerSet(api, entry.getValue());
            });

            cache.getAllPlaces().entrySet().stream().forEach(entry -> {
                addPlaceMarker(entry.getValue());
            });
        });
    }

    //------------------------------------------------------------------------------------------------------------------

    public void addCategoryMarkerSet(BlueMapAPI api, Category category) {
        System.out.println("Bluemap 3");
        if(category.getBluemapIcon() == null) return;
        Place firstPlace = category.getPlaces().stream().findFirst().orElse(null);
        World placeWorld = firstPlace.getLocation().getWorld();
        if(firstPlace != null) {
            System.out.println("Bluemap 4");
            api.getWorld(placeWorld).ifPresent(world -> {
                MarkerSet markerSet = MarkerSet.builder()
                        .label(category.getName())
                        .build();
                for(BlueMapMap map : world.getMaps()) {
                    System.out.println("Bluemap 5");
                    map.getMarkerSets().put(TextUtils.getBluemapId(category.getName()), markerSet);
                    markerSetMap.put(TextUtils.getBluemapId(category.getName()), markerSet);
                    System.out.println(TextUtils.getBluemapId(category.getName()));
                }
            });
        } else {
            System.out.println(category.getName() + " has no markers");
        }
    }

    public void removeCategoryMarkerSet(Category category) {

    }

    public void addPlaceMarker(Place place) {
        System.out.println("Bluemap 6");
        Location location = place.getLocation();
        Category highestCategory = null;

        for(Category category : place.getCategories()) {
            System.out.println("Bluemap 7");
            if(highestCategory == null || category.getPriority() > highestCategory.getPriority()) {
                highestCategory = category;
            }
        }

        System.out.println("Bluemap 8 " + highestCategory.getBluemapIcon());
        if(highestCategory == null || highestCategory.getBluemapIcon() == null) return;

        System.out.println("Bluemap 9");
        POIMarker marker = POIMarker.builder()
                .label(place.getName())
                .position(location.getX(), location.getY(), location.getZ())
                .maxDistance(1000)
                .build();
        marker.setIcon(highestCategory.getBluemapIcon(), 0, 0);
        markerMap.put(place.getName(), marker);
        MarkerSet markerSet = markerSetMap.get(highestCategory.getName());
        markerSet.getMarkers().put(TextUtils.getBluemapId(highestCategory.getName()), marker);
        System.out.println(TextUtils.getBluemapId(highestCategory.getName()));
        System.out.println("Bluemap 10");
    }

    public void removePlaceMarker(Place place) {

    }

}
