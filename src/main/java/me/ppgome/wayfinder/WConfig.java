package me.ppgome.wayfinder;

import org.bukkit.configuration.file.FileConfiguration;

public class WConfig {

    private Wayfinder plugin;

    public String PREFIX;

    /*
    MESSAGES
     */
    // Place
    //   Normal
    public String PLACE_REACHED_DESTINATION;
    public String PLACE_ROUTING_TO;
    public String PLACE_ALREADY_ROUTING;
    public String PLACE_DOES_NOT_EXIST;

    //   Editing
    public String PLACE_CATEGORY_EDITING_NAME;
    public String PLACE_CATEGORY_EDITING_PRIORITY;
    public String PLACE_CATEGORY_EDITING_ICON;
    public String PLACE_CATEGORY_ALREADY_EXISTS;

    public String PLACE_PLACE_EDITING_NAME;
    public String PLACE_PLACE_EDITING_BUILDERS;
    public String PLACE_PLACE_EDITING_EXPIRES;
    public String PLACE_PLACE_ALREADY_EXISTS;

    // Home
    //   Normal

    //   Editing
    public String HOME_HOME_EDITING_NAME;

    // Warp
    // Normal
    public String WARP_WARP_SUCCESS;
    public String WARP_WARP_DOES_NOT_EXIST;
    // Editing
    public String WARP_WARP_EDITING_NAME;
    public String WARP_WARP_EDITING_BUILDERS;
    public String WARP_WARP_EDITING_PERMISSION;
    public String WARP_WARP_ALREADY_EXISTS;

    // Reload
    public String RELOAD_SUCCESS;

    /*
    GUI
     */
    // Place
    public String GUI_PLACE_MAIN;
    public String GUI_PLACE_CAT_BUILDER;
    public String GUI_PLACE_PLACE_BUILDER;
    public String GUI_PLACE_CAT_LIST;
    public String GUI_PLACE_PLACE_LIST;

    // Home
    public String GUI_HOME_HOME_BUILDER;

    // Warp
    public String GUI_WARP_MAIN;
    public String GUI_WARP_BUILDER;

    // Place default categories
    public boolean ENABLE_NETHER_PORTALS;
    public boolean ENABLE_END_PORTALS;
    public boolean ENABLE_PLAYERS;

    public WConfig(Wayfinder plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        String errorMessage = "<red>The config option for this message is missing. Let an admin know!</red>";

        PREFIX = config.getString("prefix", "<blue><b>[</b><aqua>Wayfinder</aqua><b>] ");

        /*
        MESSAGES
         */
        
        // Place
        //  Normal
        String root = "messages.place.normal.";
        PLACE_REACHED_DESTINATION = config.getString(root + "reachedDestination", errorMessage);
        PLACE_ROUTING_TO = config.getString(root + "routingTo", errorMessage);
        PLACE_ALREADY_ROUTING = config.getString(root + "alreadyRouting", errorMessage);
        PLACE_DOES_NOT_EXIST = config.getString(root + "doesNotExist", errorMessage);

        //  Editing
        // -- category
        root = "messages.place.editing.category.";
        PLACE_CATEGORY_EDITING_NAME = config.getString(root + "name", errorMessage);
        PLACE_CATEGORY_EDITING_PRIORITY = config.getString(root + "priority", errorMessage);
        PLACE_CATEGORY_EDITING_ICON = config.getString(root + "icon", errorMessage);
        PLACE_CATEGORY_ALREADY_EXISTS = config.getString(root + "alreadyExists", errorMessage);

        // -- place
        root = "messages.place.editing.place.";
        PLACE_PLACE_EDITING_NAME = config.getString(root + "name", errorMessage);
        PLACE_PLACE_EDITING_BUILDERS = config.getString(root + "builders", errorMessage);
        PLACE_PLACE_EDITING_EXPIRES = config.getString(root + "expires", errorMessage);
        PLACE_PLACE_ALREADY_EXISTS = config.getString(root + "alreadyExists", errorMessage);

        // Home
        //  Normal

        //  Editing
        root = "messages.home.editing.";
        HOME_HOME_EDITING_NAME = config.getString(root + "name", errorMessage);

        // Warp
        //  Normal
        root = "messages.warp.normal.";
        WARP_WARP_SUCCESS = config.getString(root + "success", errorMessage);
        WARP_WARP_DOES_NOT_EXIST = config.getString(root + "doesNotExist", errorMessage);

        //  Editing
        root = "messages.warp.editing.";
        WARP_WARP_EDITING_NAME = config.getString(root + "name", errorMessage);
        WARP_WARP_EDITING_BUILDERS = config.getString(root + "builders", errorMessage);
        WARP_WARP_EDITING_PERMISSION = config.getString(root + "permission", errorMessage);
        WARP_WARP_ALREADY_EXISTS = config.getString(root + "alreadyExists", errorMessage);

        // Reload
        root = "messages.reload.";
        RELOAD_SUCCESS = config.getString(root + "success", errorMessage);


        /*
        GUI
         */
        // Place
        root = "gui.title.place.";
        GUI_PLACE_MAIN = config.getString(root + "main", "Places");
        GUI_PLACE_CAT_BUILDER = config.getString(root + "categoryBuilder", "Category Builder");
        GUI_PLACE_PLACE_BUILDER = config.getString(root + "placeBuilder", "Place Builder");
        GUI_PLACE_CAT_LIST = config.getString(root + "categoryList", "Selecting categories for <place>");
        GUI_PLACE_PLACE_LIST = config.getString(root + "placeList", "Places in <category>");

        // Home
        root = "gui.title.home";
        GUI_HOME_HOME_BUILDER = config.getString(root + "homeBuilder", "New home");

        // Warp
        root = "gui.title.warp";
        GUI_WARP_MAIN = config.getString(root + "main", "Warps");
        GUI_WARP_BUILDER = config.getString(root + "build", "Warp builder");

        // Place default categories
        root = "place.";
        ENABLE_NETHER_PORTALS = config.getBoolean(root + "enableNetherPortals", true);
        ENABLE_END_PORTALS = config.getBoolean(root + "enableEndPortals", true);
        ENABLE_PLAYERS = config.getBoolean(root + "enablePlayers", true);

    }

}
