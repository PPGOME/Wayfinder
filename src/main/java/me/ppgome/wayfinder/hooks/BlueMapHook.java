//package me.ppgome.wayfinder.hooks;
//
//import de.bluecolored.bluemap.api.BlueMapAPI;
//import de.bluecolored.bluemap.api.BlueMapMap;
//import de.bluecolored.bluemap.api.markers.Marker;
//import de.bluecolored.bluemap.api.markers.MarkerSet;
//import de.bluecolored.bluemap.api.markers.POIMarker;
//import me.ppgome.wayfinder.Cache;
//import me.ppgome.wayfinder.Wayfinder;
//import me.ppgome.wayfinder.database.place.Category;
//import me.ppgome.wayfinder.database.place.Place;
//import me.ppgome.wayfinder.text.TextUtils;
//import org.bukkit.Location;
//
//import java.util.HashMap;
//import java.util.Optional;
//
//public class BlueMapHook {
//
//    Wayfinder plugin;
//    Cache cache;
//    Optional<BlueMapAPI> blueMapAPI = BlueMapAPI.getInstance();
//
//    HashMap<String, MarkerSet> marketSetMap  = new HashMap<>();
//    HashMap<String, Marker> markerMap  = new HashMap<>();
//
//    public BlueMapHook(Wayfinder plugin) {
//        this.plugin = plugin;
//        this.cache = plugin.getCache();
//
//        BlueMapAPI.onEnable(api -> {
//            cache.getAllCategories().entrySet().stream().forEach(entry -> {
//                addCategoryMarkerSet(entry.getValue());
//            });
//
//            cache.getAllPlaces().entrySet().stream().forEach(entry -> {
//                addPlaceMarker(entry.getValue());
//            });
//        });
//    }
//
//    //------------------------------------------------------------------------------------------------------------------
//
//    public void addCategoryMarkerSet(Category category) {
//        Place firstPlace = category.getPlaces().stream().findFirst().orElse(null);
//        if(firstPlace != null) {
//            blueMapAPI.get().getWorld(category.getPlaces()).ifPresent(world -> {
//                for(BlueMapMap map : world.getMaps()) {
//                    map.getMarkerSets().put(TextUtils.getBluemapId(firstCategory.getName()), markerSet);
//                }
//            });
//        } else {
//            System.out.println(category.getName() + " has no markers");
//        }
//    }
//
//    public void removeCategoryMarkerSet(Category category) {
//
//    }
//
//    public void addPlaceMarker(Place place) {
//        Location location = place.getLocation();
//        Category firstCategory = place.getCategories().stream().findFirst().orElse(null);
//        POIMarker marker = POIMarker.builder()
//                .label(place.getName())
//                .position(location.getX(), location.getY(), location.getZ())
//                .maxDistance(1000)
//                .build();
//        if(firstCategory != null) {
//            marker.setIcon(firstCategory.getBluemapIcon(), 0, 0);
//            markerMap.put(place.getName(), marker);
//            MarkerSet markerSet = marketSetMap.get(firstCategory.getName());
//            markerSet.getMarkers().put(TextUtils.getBluemapId(place.getName()), marker);
//        }
//    }
//
//    public void removePlaceMarker(Place place) {
//
//    }
//
//}
