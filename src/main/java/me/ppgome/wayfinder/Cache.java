package me.ppgome.wayfinder;

import me.ppgome.wayfinder.database.DatabaseHandler;
import me.ppgome.wayfinder.database.PlayerMeta;
import me.ppgome.wayfinder.database.Warp;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.hooks.BlueMapHook;
import me.ppgome.wayfinder.navigation.ParticleNavigator;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class Cache {

    private final Wayfinder plugin;
    private final DatabaseHandler database;
    private final BlueMapHook blueMapHook;

    private final HashMap<UUID, PlayerMeta> playerMetaMap;

    private final HashMap<String, Place> places;
    private final HashMap<String, Category> placeCategories;
    private final HashMap<String, Place> pinnedPlaces;

    private final HashMap<String, Warp> warps;

    private final Set<Place> netherPortalPlaces;
    private final Set<Place> endPortalPlaces;

    private final HashMap<UUID, WGui> editingGui;
    private final Set<UUID> awaitingChat;
    private final HashMap<UUID, ParticleNavigator> routing;

    private final HashMap<UUID, Location> back;

    //------------------------------------------------------------------------------------------------------------------

    public Cache(Wayfinder plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        this.blueMapHook = plugin.getBlueMapHook();
        this.playerMetaMap = new HashMap<>();
        this.places = new HashMap<>();
        this.placeCategories = new HashMap<>();
        this.warps = new HashMap<>();
        this.pinnedPlaces = new HashMap<>();
        this.netherPortalPlaces = new HashSet<>();
        this.endPortalPlaces = new HashSet<>();
        this.editingGui = new HashMap<>();
        this.awaitingChat = new HashSet<>();
        this.routing = new HashMap<>();
        this.back = new HashMap<>();
    }

    // Places ----------------------------------------------------------------------------------------------------------

    public void addPlace(Place place) {
        places.put(key(place.getName()), place);
    }

    public Place getPlace(String name) {
        return places.get(key(name));
    }

    public Map<String, Place> getAllPlaces() {
        return places;
    }

    public void removePlace(String name) {
        Place place = getPlace(key(name));
        places.remove(key(name));
        if(place != null) {
            if(!place.getCategories().isEmpty()) {
                for(Category category : place.getCategories()) {
                    category.removePlace(place);
                }
            }
            blueMapHook.removePlaceMarker(place.getLocation(), place.getName());
            database.getPlaceTable().delete(place);
        }
    }

    // Place Categories ------------------------------------------------------------------------------------------------

    public void addCategory(Category category) {
        placeCategories.put(key(category.getName()), category);
        database.getCategoryTable().save(category);
    }

    public void updateCategory(Category category) {
        database.getCategoryTable().save(category);
    }

    public Category getCategory(String name) {
        return placeCategories.get(key(name));
    }

    public Map<String, Category> getAllCategories() {
        return placeCategories;
    }

    public void removeCategory(Category category) {
        placeCategories.remove(key(category.getName()));
        database.getCategoryTable().delete(category);
    }

    // Pinned Places ---------------------------------------------------------------------------------------------------

    public void addPinnedPlace(Place place) {
        pinnedPlaces.put(key(place.getName()), place);
    }

    public Place getPinnedPlace(String name) {
        return pinnedPlaces.get(key(name));
    }

    public Map<String, Place> getAllPinnedPlaces() {
        return pinnedPlaces;
    }

    public void removePinnedPlace(String name) {
        pinnedPlaces.remove(key(name));
    }

    // Warps -----------------------------------------------------------------------------------------------------------

    public void addWarp(Warp warp) {
        warps.put(key(warp.getName()), warp);
    }

    public Warp getWarp(String name) {
        return warps.get(key(name));
    }

    public Map<String, Warp> getAllWarps() {
        return warps;
    }

    public void removeWarp(String name) {
        Warp warp =  getWarp(key(name));
        warps.remove(key(name));
        database.getWarpTable().delete(warp);
    }

    // Nether Portal Places --------------------------------------------------------------------------------------------

    public void addNetherPortalPlace(Place place) {
        netherPortalPlaces.add(place);
    }

    public Set<Place> getAllNetherPortalPlaces() {
        return netherPortalPlaces;
    }

    public void removeNetherPortalPlace(Place place) {
        netherPortalPlaces.remove(place);
    }

    // End Portal Places -----------------------------------------------------------------------------------------------

    public void addEndPortalPlace(Place place) {
        endPortalPlaces.add(place);
    }

    public Set<Place> getAllEndPortalPlaces() {
        return endPortalPlaces;
    }

    public void removeEndPortalPlace(Place place) {
        endPortalPlaces.remove(place);
    }

    // PlayerMeta by UUID ----------------------------------------------------------------------------------------------

    public void addPlayerMeta(PlayerMeta playerMeta) {
        playerMetaMap.put(playerMeta.getUuid(), playerMeta);
    }

    public PlayerMeta getPlayerMeta(UUID uuid) {
        return playerMetaMap.get(uuid);
    }

    // Editing GUI -------------------------------------------=---------------------------------------------------------

    public void addEditingGui(UUID uuid, WGui wGui) {
        editingGui.put(uuid, wGui);
    }

    public boolean isEditingGui(UUID uuid) {
        return editingGui.containsKey(uuid);
    }

    public void reopenGui(Player player) {
        UUID uuid = player.getUniqueId();
        editingGui.get(uuid).display(player);
        removeEditingGui(uuid);
    }

    public WGui getGui(UUID uuid) {
        return editingGui.get(uuid);
    }

    public void removeEditingGui(UUID uuid) {
        editingGui.remove(uuid);
    }

    // Awaiting Chat -------------------------------------------=-------------------------------------------------------

    public void addAwaitingChat(UUID uuid) {
        awaitingChat.add(uuid);
    }

    public boolean isAwaitingChat(UUID uuid) {
        return awaitingChat.contains(uuid);
    }

    public void removeAwaitingChat(UUID uuid) {
        awaitingChat.remove(uuid);
        removeEditingGui(uuid);
    }

    // Routing -------------------------------------------=-------------------------------------------------------------

    public void addRouting(UUID uuid, ParticleNavigator particleNavigator) {
        routing.put(uuid, particleNavigator);
    }

    public boolean isRouting(UUID uuid) {
        return routing.containsKey(uuid);
    }

    public void removeRouting(UUID uuid) {
        routing.get(uuid).cancelTask();
        routing.remove(uuid);
    }

    // Back -------------------------------------------=----------------------------------------------------------------

    public void addBack(UUID uuid, Location location) {
        back.put(uuid, location);
    }

    public Location getBack(UUID uuid) {
        return back.get(uuid);
    }

    public boolean isBack(UUID uuid) {
        return back.containsKey(uuid);
    }

    public void removeBack(UUID uuid) {
        back.remove(uuid);
    }

    // Util -------------------------------------------=----------------------------------------------------------------

    private static String key(String name) {
        return name == null ? null : name.toLowerCase(Locale.ROOT);
    }

}
