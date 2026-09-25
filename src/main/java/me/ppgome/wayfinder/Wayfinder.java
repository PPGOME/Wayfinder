package me.ppgome.wayfinder;

import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.commands.CommandRegistry;
import me.ppgome.wayfinder.commands.generic.WayfinderCommand;
import me.ppgome.wayfinder.commands.home.HomeCommand;
import me.ppgome.wayfinder.commands.place.PlaceCommand;
import me.ppgome.wayfinder.commands.warp.WarpCommand;
import me.ppgome.wayfinder.database.DatabaseHandler;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.guis.GuiUtils;
//import me.ppgome.wayfinder.hooks.BlueMapHook;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Wayfinder extends JavaPlugin {

    public static Wayfinder PLUGIN;

    private DatabaseHandler database;
    private Cache cache;
    private WConfig config;

//    BlueMapHook blueMapHook;

    // Commands
    private CommandRegistry commandRegistry;

    private PlaceCommand placeCommand;
    private HomeCommand homeCommand;
    private WarpCommand warpCommand;
    private WayfinderCommand wayfinderCommand;

    @Override
    public void onEnable() {
        // Plugin startup logic
        PLUGIN = this;
        config = new WConfig(this);
        database = new DatabaseHandler(this);
        database.setupDatabase();
        this.cache = new Cache(this);
        database.setCache(cache);
        database.loadDatabaseData();
        initializeCommands();
        registerCommands();
        getServer().getPluginManager().registerEvents(new WListener(this, cache), this);
        defaultCategories();
//        loadBlueMap();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Commands --------------------------------------------------------------------------------------------------------

    private void initializeCommands() {
        placeCommand = new PlaceCommand(this);
        homeCommand = new HomeCommand(this);
        warpCommand = new WarpCommand(this);
        wayfinderCommand = new WayfinderCommand(this);
    }

    private void registerCommands() {
        commandRegistry = new CommandRegistry();
        commandRegistry.register("place", placeCommand);
        commandRegistry.register("home", homeCommand);
        commandRegistry.register("warp", warpCommand);
        commandRegistry.register("wayfinder", wayfinderCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String name, String @NotNull [] args) {
        if(!(sender instanceof Player player)) {
            return false;
        }
        CommandHandler handler = commandRegistry.getHandler(command.getName());
        if(handler != null) {
            return handler.execute(player, name, args);
        }
        return false;
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            return java.util.Collections.emptyList();
        }

        CommandHandler handler = commandRegistry.getHandler(command.getName());
        if (handler != null) {
            return handler.tabComplete((Player) sender, alias, args);
        }

        return java.util.Collections.emptyList();
    }

    //------------------------------------------------------------------------------------------------------------------

    public void defaultCategories() {
        String name;
        if(config.ENABLE_NETHER_PORTALS) {
            name = "Nether Portals";
            if(cache.getCategory(name) != null) {
                createDefaultCategory(name);
            }
        }
        if(config.ENABLE_NETHER_PORTALS) {
            name = "End Portals";
            if(cache.getCategory(name) != null) {
                createDefaultCategory(name);
            }
        }
        if(config.ENABLE_PLAYERS) {
            name = "Players";
            if(cache.getCategory(name) != null) {
                createDefaultCategory(name);
            }
        }
    }

    private void createDefaultCategory(String name) {
        ItemStack icon = GuiUtils.getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBiZmMyNTc3ZjZlMjZjNmM2ZjczNjVjMmM0MDc2YmNjZWU2NTMxMjQ5ODkzODJjZTkzYmNhNGZjOWUzOWIifX19", Component.text("Nether Portals"));
        Category category = new Category(name, 50, icon);
        database.getCategoryTable().save(category);
        cache.addCategory(category);
    }

    //------------------------------------------------------------------------------------------------------------------

//    private void loadBlueMap() {
//        Plugin bluemapPlugin =  getServer().getPluginManager().getPlugin("BlueMap");
//        if(bluemapPlugin != null) {
//            this.blueMapHook = new BlueMapHook(this);
//        }
//    }

    //------------------------------------------------------------------------------------------------------------------

    public DatabaseHandler getDatabase() {
        return database;
    }

    public Cache getCache() {
        return cache;
    }

    public WConfig getConfiguration() {
        return config;
    }

}
