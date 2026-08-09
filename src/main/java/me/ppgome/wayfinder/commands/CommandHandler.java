package me.ppgome.wayfinder.commands;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public interface CommandHandler {

    /**
     * Executes a command.
     *
     * @param player The player executing the command.
     * @param name The name of the command.
     * @param args The arguments of the command.
     * @return {@code true} if the command was successful, {@code false} if not.
     */
    boolean execute(Player player, String name, String[] args);

    default List<String> tabComplete(Player player, String name, String[] args) {
        return Collections.emptyList();
    }
}
