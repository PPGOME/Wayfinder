package me.ppgome.wayfinder.commands.warp;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.guis.place.PlaceMainGui;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandHandler {

    Wayfinder plugin;
    Cache cache;
    WConfig config;

    public WarpCommand(Wayfinder plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
        this.config = plugin.getConfiguration();
    }

    @Override
    public boolean execute(Player player, String name, String[] args) {
        if(cache.isEditingGui(player.getUniqueId())) {
            cache.reopenGui(player);
            return true;
        }
        new PlaceMainGui(config, cache).display(player);
        return true;
    }

}
