package me.ppgome.wayfinder;

import me.ppgome.wayfinder.database.DatabaseHandler;
import me.ppgome.wayfinder.database.PlayerMeta;
import me.ppgome.wayfinder.database.PlayerMetaTable;
import me.ppgome.wayfinder.database.Warp;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.navigation.ParticleNavigator;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.*;

public class Cache {

    private Wayfinder plugin;
    private DatabaseHandler database;

    private HashMap<UUID, PlayerMeta> playerMetaMap;

    private HashMap<String, Place> places;
    private HashMap<String, Category> placeCategories;
    private HashMap<String, Place> pinnedPlaces;

    private HashMap<String, Warp> warps;

    private Set<Place> overworldNetherPortals;
    private Set<Place> netherNetherPortals;

    private HashMap<UUID, PlayerMeta> playerMetaByUuid;
    private HashMap<String, PlayerMeta> playerMetaByName;

    private HashMap<UUID, WGui> editingGui;
    private Set<UUID> awaitingChat;
    private HashMap<UUID, ParticleNavigator> routing;

    //------------------------------------------------------------------------------------------------------------------

    public Cache(Wayfinder plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        this.playerMetaMap = new HashMap<>();
        this.places = new HashMap<>();
        this.placeCategories = new HashMap<>();
        this.warps = new HashMap<>();
        this.pinnedPlaces = new HashMap<>();
        this.overworldNetherPortals = new HashSet<>();
        this.netherNetherPortals = new HashSet<>();
        this.playerMetaByUuid = new HashMap<>();
        this.playerMetaByName = new HashMap<>();
        this.editingGui = new HashMap<>();
        this.awaitingChat = new HashSet<>();
        this.routing = new HashMap<>();
    }

    // Places ----------------------------------------------------------------------------------------------------------

    public void addPlayerMeta(PlayerMeta playerMeta) {
        playerMetaMap.put(playerMeta.getUuid(), playerMeta);
    }

    public PlayerMeta getPlayerMeta(UUID uuid) {
        return playerMetaMap.get(uuid);
    }

    // Places ----------------------------------------------------------------------------------------------------------

    public void addPlace(Place place) {
        places.put(place.getName(), place);
    }

    public Place getPlace(String name) {
        return places.get(name);
    }

    public Map<String, Place> getAllPlaces() {
        return places;
    }

    public void removePlace(String name) {
        places.remove(name);
    }

    // Place Categories ------------------------------------------------------------------------------------------------

    public void addCategory(Category category) {
        placeCategories.put(category.getName(), category);
        database.getCategoryTable().save(category);
    }

    public void updateCategory(Category category) {
        database.getCategoryTable().save(category);
    }

    public Category getCategory(String name) {
        return placeCategories.get(name);
    }

    public Map<String, Category> getAllCategories() {
        return placeCategories;
    }

    public void removeCategory(Category category) {
        placeCategories.remove(category.getName());
        database.getCategoryTable().delete(category);
    }

    // Pinned Places ---------------------------------------------------------------------------------------------------

    public void addPinnedPlace(Place place) {
        pinnedPlaces.put(place.getName(), place);
    }

    public Place getPinnedPlace(String name) {
        return pinnedPlaces.get(name);
    }

    public Map<String, Place> getAllPinnedPlaces() {
        return pinnedPlaces;
    }

    public void removePinnedPlace(String name) {
        pinnedPlaces.remove(name);
    }

    // Warps -----------------------------------------------------------------------------------------------------------

    public void addWarp(Warp warp) {
        warps.put(warp.getName(), warp);
    }

    public Warp getWarp(String name) {
        return warps.get(name);
    }

    public Map<String, Warp> getAllWarps() {
        return warps;
    }

    public void removeWarp(String name) {
        Warp warp =  getWarp(name);
        warps.remove(name);
        database.getWarpTable().delete(warp);
    }

    // Overworld Nether Portals ----------------------------------------------------------------------------------------



    // Nether Nether Portals -------------------------------------------------------------------------------------------



    // PlayerMeta by UUID ----------------------------------------------------------------------------------------------



    // PlayerMeta by Name ----------------------------------------------------------------------------------------------



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

}
