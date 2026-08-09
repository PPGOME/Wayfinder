package me.ppgome.wayfinder.commands;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private final Map<String, CommandHandler> commands = new HashMap<>();

    /**
     * Register a command and store it for recalling
     * @param commandName What the player types in chat
     * @param commandHandler The instance of that command's class
     */
    public void register(String commandName, CommandHandler commandHandler) {
        commands.put(commandName.toLowerCase(), commandHandler);
    }

    /**
     * Get the instance of a command's class
     * @param commandName What the player types in chat
     * @return The instance of that command's class
     */
    public CommandHandler getHandler(String commandName) {
        return commands.get(commandName.toLowerCase());
    }

}
