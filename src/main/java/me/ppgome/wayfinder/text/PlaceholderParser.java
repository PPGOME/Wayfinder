package me.ppgome.wayfinder.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a builder class for replacing placeholders in the plugin's configurable messages.
 */
public class PlaceholderParser {
    /**
     * The message being parsed.
     */
    private final String message;
    /**
     * The list of placeholders being replaced.
     */
    private final List<TagResolver.Single> placeholders = new ArrayList<>();
    /**
     * The instance of the MiniMessage API.
     */
    private final MiniMessage mm = MiniMessage.miniMessage();

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes the builder.
     *
     * @param message The message that will be parsed
     */
    private PlaceholderParser(String message) { this.message = message; }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Loads the message that contains placeholders into the object.
     *
     * @param message The message that will be parsed
     * @return The parsed message
     */
    public static PlaceholderParser of(String message) { return new PlaceholderParser(message); }

    /**
     * Adds a check for the <player> tag in the message and replaces it.
     *
     * @param name The name of the player that will replace <player>
     * @return The parsed message
     */
    public PlaceholderParser player(String name) {
        placeholders.add(Placeholder.component("player", Component.text(name)));
        return this;
    }

    /**
     * Adds a check for the <prefix> tag in the message and replaces it.
     *
     * @param prefix The prefix that will replace <prefix>
     * @return The parsed message
     */
    public PlaceholderParser prefix(String prefix) {
        placeholders.add(Placeholder.component("prefix", mm.deserialize(prefix)));
        return this;
    }

    /**
     * Adds a check for the <category> tag in the message and replaces it.
     *
     * @param category The name of the category that will replace <category>
     * @return The parsed message
     */
    public PlaceholderParser category(String category) {
        placeholders.add(Placeholder.component("category", mm.deserialize(category)));
        return this;
    }

    /**
     * Adds a check for the <place> tag in the message and replaces it.
     *
     * @param place The name of the place that will replace <place>
     * @return The parsed message
     */
    public PlaceholderParser place(String place) {
        placeholders.add(Placeholder.component("place", mm.deserialize(place)));
        return this;
    }

    /**
     * Parses the placeholders provided in the previous methods.
     *
     * @return The parsed message.
     */
    public Component parse() {
        return mm.deserialize(message, placeholders.toArray(new TagResolver.Single[0]));
    }

}
