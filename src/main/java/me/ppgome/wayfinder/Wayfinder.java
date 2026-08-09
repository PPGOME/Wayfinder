package me.ppgome.wayfinder;

import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.commands.CommandRegistry;
import me.ppgome.wayfinder.commands.home.HomeCommand;
import me.ppgome.wayfinder.commands.place.PlaceCommand;
import me.ppgome.wayfinder.database.DatabaseHandler;
import me.ppgome.wayfinder.database.place.Category;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Wayfinder extends JavaPlugin {

    public static Wayfinder PLUGIN;

    private DatabaseHandler database;
    private Cache cache;
    private WConfig config;

    // Commands
    private CommandRegistry commandRegistry;

    private PlaceCommand placeCommand;
    private HomeCommand homeCommand;

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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Commands --------------------------------------------------------------------------------------------------------

    private void initializeCommands() {
        placeCommand = new PlaceCommand(this);
        homeCommand = new HomeCommand(this);
    }

    private void registerCommands() {
        commandRegistry = new CommandRegistry();
        commandRegistry.register("place", placeCommand);
        commandRegistry.register("home", homeCommand);
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

    public void addPlaceCategory(Category category) {
        database.getCategoryTable().save(category);
        cache.addCategory(category);
    }

    public void removePlaceCategory() {

    }

    public void addPlace() {

    }

    public void removePlace() {

    }

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
