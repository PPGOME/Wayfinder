package me.ppgome.wayfinder.commands.teleport;

import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.text.PlaceholderParser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class BackCommand implements CommandHandler {

    Wayfinder plugin;
    Cache cache;
    WConfig config;

    public BackCommand(Wayfinder plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
        this.config = plugin.getConfiguration();
    }

    @Override
    public boolean execute(Player player, String name, String[] args) {
        UUID uuid = player.getUniqueId();
        if(cache.isBack(uuid)) {
            player.teleport(cache.getBack(uuid));
            player.sendMessage(PlaceholderParser.of("<green>Teleported to last known location.</green>").prefix(config.PREFIX).parse());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(Player player, String name, String[] args) {
        return CommandHandler.super.tabComplete(player, name, args);
    }

}
