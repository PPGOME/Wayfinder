package me.ppgome.wayfinder.commands.home;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.commands.CommandHandler;
import me.ppgome.wayfinder.database.Home;
import me.ppgome.wayfinder.database.PlayerMeta;
import me.ppgome.wayfinder.guis.home.HomesGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class HomeCommand implements CommandHandler {

    private final Wayfinder plugin;
    private final Cache cache;

    public HomeCommand(Wayfinder plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
    }

    @Override
    public boolean execute(Player player, String name, String[] args) {

        int argslength = args.length;

        PlayerMeta playerMeta = cache.getPlayerMeta(player.getUniqueId());

        if(argslength == 1) {
            // Check for another player's homes
            if(args[0].substring(0, 2).equalsIgnoreCase("p:")) {
                String playerName = args[0].substring(2);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
                if(targetPlayer.hasPlayedBefore()) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                       PlayerMeta targetPlayerMeta = cache.getPlayerMeta(targetPlayer.getUniqueId());
                    });
                }
            }
            // Check homes of player running command
            else {
                Home home = playerMeta.getHome(args[0]);
                if(home != null) {
                    player.teleport(home.getLocation());
                    player.sendMessage(Component.text("Welcome home! (", NamedTextColor.BLUE)
                            .append(Component.text(home.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(")", NamedTextColor.BLUE)));
                }
            }
        } else if(argslength == 0) {
            if(cache.isEditingGui(player.getUniqueId())) {
                cache.reopenGui(player);
                return true;
            }
            new HomesGui(playerMeta, true).display(player);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(Player player, String name, String[] args) {

        PlayerMeta playerMeta = cache.getPlayerMeta(player.getUniqueId());

        return Collections.emptyList();
    }
}
