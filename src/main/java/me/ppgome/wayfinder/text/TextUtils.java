package me.ppgome.wayfinder.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TextUtils {

    private static MiniMessage mm =  MiniMessage.miniMessage();

    public static Component stripDecoration(Component component) {
        return component.color(NamedTextColor.WHITE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static Component stripDecoration(String string) {
        return Component.text(string, NamedTextColor.WHITE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static Component parseMiniMessage(Component component) {
        return mm.deserialize(PlainTextComponentSerializer.plainText().serialize(component));
    }

    public static Component parseMiniMessage(String string) {
        return mm.deserialize(string);
    }

    public static String getRawName(Component name) {
        return mm.serialize(name);
    }

    public static long stringToMilliseconds(String timeExpression) {
        String[] parts = timeExpression.split("(?<=[dhm])");

        int days = 0;
        int hours = 0;
        int minutes = 0;
        for (String part : parts) {
            int value = Integer.parseInt(part.replaceAll("[dhm]", ""));
            if (part.endsWith("d")) days = value;
            else if (part.endsWith("h")) hours = value;
            else if (part.endsWith("m")) minutes = value;
        }
        return System.currentTimeMillis() + Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).toMillis();
    }

    public static String millisecondsToReadableTime(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")) + " UTC";
    }

    public static String getBluemapId(String name) {
        return name.replace(' ', '-').toLowerCase();
    }

    public static Component locationToComponent(Location location) {
        return Component.text("x: " + String.format(Locale.ROOT, "%.2f", location.getX()), NamedTextColor.RED)
                .append(Component.text(" y: " + String.format(Locale.ROOT, "%.2f", location.getY()), NamedTextColor.GREEN))
                .append(Component.text(" z: " + String.format(Locale.ROOT, "%.2f", location.getZ()), NamedTextColor.YELLOW));
    }

}
