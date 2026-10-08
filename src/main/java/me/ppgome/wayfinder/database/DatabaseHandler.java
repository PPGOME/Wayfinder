package me.ppgome.wayfinder.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.*;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseHandler {

    private final Wayfinder plugin;
    private Cache cache;

    private static final String DATABASE_URL = "jdbc:sqlite:plugins/Wayfinder/Wayfinder.db";

    /**
     * The connection source for the SQLite database.
     * This is used to establish connections to the database for performing CRUD operations.
     */
    private ConnectionSource connectionSource;

    private Dao<Home, Integer> homeDao;
    private Dao<PlayerMeta, UUID> playerMetaDao;
    private Dao<TeleportRelationship, Integer> teleportRelationshipDao;
    private Dao<Warp, String> warpDao;
    private Dao<Waypoint, Integer> waypointDao;
    private Dao<Place, String> placeDao;
    private Dao<NetherPortalLink, String> netherPortalLinkDao;
    private Dao<Category, String> categoryDao;
    private Dao<CategorizedPlace, Integer> categorizedPlaceDao;

    private HomeTable homeTable;
    private PlayerMetaTable playerMetaTable;
    private TeleportRelationshipTable teleportRelationshipTable;
    private WarpTable warpTable;
    private WaypointTable waypointTable;
    private PlaceTable placeTable;
    private NetherPortalLinkTable netherPortalLinkTable;
    private CategoryTable categoryTable;
    private CategorizedPlaceTable categorizedPlaceTable;

    //------------------------------------------------------------------------------------------------------------------

    public DatabaseHandler(Wayfinder plugin) {
        this.plugin = plugin;
    }

    //------------------------------------------------------------------------------------------------------------------

    public void setupDatabase() {
        try {
            // Initialize the connection source
            connectionSource = new JdbcConnectionSource(DATABASE_URL);

            // Create DAOs
            homeDao = DaoManager.createDao(connectionSource, Home.class);
            playerMetaDao = DaoManager.createDao(connectionSource, PlayerMeta.class);
            teleportRelationshipDao = DaoManager.createDao(connectionSource, TeleportRelationship.class);
            warpDao = DaoManager.createDao(connectionSource, Warp.class);
            waypointDao = DaoManager.createDao(connectionSource, Waypoint.class);
            placeDao = DaoManager.createDao(connectionSource, Place.class);
            netherPortalLinkDao = DaoManager.createDao(connectionSource, NetherPortalLink.class);
            categoryDao = DaoManager.createDao(connectionSource, Category.class);
            categorizedPlaceDao = DaoManager.createDao(connectionSource, CategorizedPlace.class);

            // Initialize table handlers
            homeTable = new HomeTable(plugin, homeDao);
            playerMetaTable = new PlayerMetaTable(plugin, playerMetaDao);
            teleportRelationshipTable = new TeleportRelationshipTable(plugin, teleportRelationshipDao);
            warpTable = new WarpTable(plugin, warpDao);
            waypointTable = new WaypointTable(plugin, waypointDao);
            placeTable = new PlaceTable(plugin, placeDao);
            netherPortalLinkTable = new NetherPortalLinkTable(plugin, netherPortalLinkDao);
            categoryTable = new CategoryTable(plugin, categoryDao);
            categorizedPlaceTable = new CategorizedPlaceTable(plugin, categorizedPlaceDao);

            // Create tables if they do not exist
            TableUtils.createTableIfNotExists(connectionSource, Home.class);
            TableUtils.createTableIfNotExists(connectionSource, PlayerMeta.class);
            TableUtils.createTableIfNotExists(connectionSource, TeleportRelationship.class);
            TableUtils.createTableIfNotExists(connectionSource, Warp.class);
            TableUtils.createTableIfNotExists(connectionSource, Waypoint.class);
            TableUtils.createTableIfNotExists(connectionSource, Place.class);
            TableUtils.createTableIfNotExists(connectionSource, NetherPortalLink.class);
            TableUtils.createTableIfNotExists(connectionSource, Category.class);
            TableUtils.createTableIfNotExists(connectionSource, CategorizedPlace.class);

        } catch(SQLException e) {
            plugin.getLogger().severe("Failed to set up database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Void> loadDatabaseData() {

        CompletableFuture<List<Category>> categoriesFuture = categoryTable.getAllCategories();
        CompletableFuture<List<Place>> placesFuture = placeTable.getAllPlaces();
        CompletableFuture<List<CategorizedPlace>> categorizedPlacesFuture = categorizedPlaceTable.getAllCategories();
        CompletableFuture<List<NetherPortalLink>> netherPortalLinksFuture = netherPortalLinkTable.getAllNetherPortalLinks();
        CompletableFuture<List<Home>>  homesFuture = homeTable.getAllHomes();
        CompletableFuture<List<Warp>> warpsFuture = warpTable.getAllWarps();

        return CompletableFuture.allOf(categoriesFuture, placesFuture, categorizedPlacesFuture,
                netherPortalLinksFuture, homesFuture, warpsFuture).thenRun(() -> {

            try {
                List<Category> categories = categoriesFuture.get();
                List<Place> places = placesFuture.get();
                List<CategorizedPlace> categorizedPlaces = categorizedPlacesFuture.get();
                List<NetherPortalLink> netherPortalLinks = netherPortalLinksFuture.get();
                List<Home> homes = homesFuture.get();
                List<Warp> warps = warpsFuture.get();

                for(Category category : categories) {
                    category.loadItem();
                    cache.addCategory(category);
                }

                for(Place place : places) {
                    try {
                        place.loadItem();
                        cache.addPlace(place);
                        if(place.isPinned()) {
                            cache.addPinnedPlace(place);
                        }
                    } catch(Exception e) {}
                }

                if(!netherPortalLinks.isEmpty()) {
                    for(NetherPortalLink netherPortalLink : netherPortalLinks) {
                        Place findPlace = cache.getPlace(netherPortalLink.getPlaceName());
                        if(findPlace != null) findPlace.setNetherPortalLink(netherPortalLink);
                    }
                }

                Place placeGainingCategory;
                Category categoryBeingApplied;

                for(CategorizedPlace categorizedPlace : categorizedPlaces) {
                    placeGainingCategory = cache.getPlace(categorizedPlace.getPlace());
                    categoryBeingApplied = cache.getCategory(categorizedPlace.getCategory());

                    if(placeGainingCategory == null || categoryBeingApplied == null) {
                        plugin.getLogger().warning("Skipping orphaned CategorizedPlace row (place='"
                                + categorizedPlace.getPlace() + "', category='" + categorizedPlace.getCategory()
                                + "') - referenced place or category no longer exists.");
                        continue;
                    }

                    placeGainingCategory.addCategory(categoryBeingApplied);
                    categoryBeingApplied.addPlace(placeGainingCategory);
                }

                for(Warp warp : warps) {
                    warp.loadItem();
                    cache.addWarp(warp);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to process data.");
            e.printStackTrace();
            return null;
        });
    }

    // Getters ---------------------------------------------------------------------------------------------------------

    public HomeTable getHomeTable() {
        return homeTable;
    }

    public PlayerMetaTable getPlayerMetaTable() {
        return playerMetaTable;
    }

    public TeleportRelationshipTable getTeleportRelationshipTable() {
        return teleportRelationshipTable;
    }

    public WarpTable getWarpTable() {
        return warpTable;
    }

    public WaypointTable getWaypointTable() {
        return waypointTable;
    }

    public PlaceTable getPlaceTable() {
        return placeTable;
    }

    public  NetherPortalLinkTable getNetherPortalLinkTable() {
        return netherPortalLinkTable;
    }

    public CategoryTable getCategoryTable() {
        return categoryTable;
    }

    public CategorizedPlaceTable getCategorizedPlaceTable() {
        return categorizedPlaceTable;
    }

    // Setters ---------------------------------------------------------------------------------------------------------

    public void setCache(Cache cache) {
        this.cache = cache;
    }
}
