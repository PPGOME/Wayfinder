package me.ppgome.wayfinder.commands.generic;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.text.PlaceholderParser;
import org.bukkit.entity.Player;

import java.util.List;

public class WayfinderCommand implements CommandHandler {

    private Wayfinder plugin;
    private Cache cache;
    private final WConfig config;

    public WayfinderCommand(Wayfinder plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
        this.config = plugin.getConfiguration();
    }

    @Override
    public boolean execute(Player player, String name, String[] args) {
        int argslength = args.length;

        if(argslength == 1) {
            if(args[0].equalsIgnoreCase("help")) {
                //TODO
            } else if(args[0].equalsIgnoreCase("reload")) {
                // Reload config.
                config.reload();
                player.sendMessage(PlaceholderParser.of(config.RELOAD_SUCCESS).prefix(config.PREFIX).parse());
            }
        }
        return false;
    }

    @Override
    public List<String> tabComplete(Player player, String name, String[] args) {
        return CommandHandler.super.tabComplete(player, name, args);
    }

}
