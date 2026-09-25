package me.ppgome.wayfinder.guis;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.Home;
import me.ppgome.wayfinder.database.Warp;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.navigation.ParticleNavigator;
import me.ppgome.wayfinder.text.PlaceholderParser;
import me.ppgome.wayfinder.text.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiUtils {

    /**
     * Get the slot placement for centering items in a GUI area.
     * @param itemCount the number of items to place
     * @param width the width of the area
     * @param height the height of the area
     * @param startSlot the starting slot of the area
     * @return an array of slot indices where the items should be placed
     */
    public static int[] getSlotPlacement(int itemCount, int width, int height, int startSlot) {

        int areaSize = width * height;
        if (itemCount > areaSize)
            throw new IllegalArgumentException("Too many items for this region");

        int startRow = startSlot / 9;
        int startCol = startSlot % 9;

        int[] areaSlots = new int[areaSize];
        int idx = 0;

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int row = startRow + r;
                int col = startCol + c;
                areaSlots[idx++] = row * 9 + col;
            }
        }

        int totalPadding = areaSize - itemCount;
        int leftPad = totalPadding / 2;

        int[] result = new int[itemCount];
        System.arraycopy(areaSlots, leftPad, result, 0, itemCount);

        return result;
    }

    /**
     * Convert a map to a list sorted by keys.
     * @param map the map to convert
     * @return a list of values sorted by keys
     * @param <T> the type of the map values
     */
    public static <T> List<T> mapToList(Map<String, T> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
    }

    /**
     * Insert gray panes into a GUI between two slots (inclusive).
     * @param gui the GUI to insert panes into
     * @param firstSlot the first slot to insert a pane into
     * @param lastSlot the last slot to insert a pane into
     */
    public static void insertGrayPanes(Gui gui, int firstSlot, int lastSlot) {

        GuiItem pane = PaperItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem();

        for(int i = firstSlot; i <= lastSlot; i++) {
            if(isBorder(i)) continue;
            gui.setItem(i, pane);
        }
    }

    public static boolean isBorder(int slot) {
        int posInRow = slot % 9;
        return posInRow == 8 || posInRow == 0;
    }

    public static void fillWhitePanes(Gui gui) {
        gui.getFiller().fill(PaperItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .asGuiItem());
    }

    public static void fillWhitePanesEditor(Gui gui) {
        gui.getFiller().fill(PaperItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .asGuiItem(event -> event.setCancelled(true)));
    }

    public static void addPlaceItem(WGui wGui, Gui gui, int slot, Place place, Player player) {
        gui.setItem(slot, GuiUtils.makePlaceItem(place));
        gui.addSlotAction(slot, event -> {
            if(event.isShiftClick()) {
                wGui.openNext(player, wGui.config, wGui.cache, new EditOrDeleteGui<>(place));
            } else {
                if(!wGui.cache.isRouting(player.getUniqueId())) {
                    new ParticleNavigator(Wayfinder.PLUGIN, player, place.getLocation(), place.getName()).startDisplaying();
                    player.sendMessage(PlaceholderParser.of(wGui.config.PLACE_ROUTING_TO).place(place.getName()).parse());
                    wGui.gui.close(player);
                } else {
                    player.sendMessage(PlaceholderParser.of(wGui.config.PLACE_ALREADY_ROUTING).place(place.getName()).parse());
                    wGui.gui.close(player);
                }
            }
        });
    }

    public static void addWarpItem(WGui wGui, Gui gui, int slot, Warp warp, Player player) {
        gui.setItem(slot, GuiUtils.makeWarpItem(warp));
        gui.addSlotAction(slot, event -> {
            if(event.isShiftClick()) {
                wGui.openNext(player, wGui.config, wGui.cache, new EditOrDeleteGui<>(warp));
            } else {
                player.teleport(warp.getLocation());
                player.sendMessage(PlaceholderParser.of(wGui.config.WARP_WARP_SUCCESS).prefix(wGui.config.PREFIX).parse());
            }
        });
    }

    public static void addHomeItem(WGui wGui, Gui gui, int slot, Home home, Player player) {
        gui.setItem(slot, GuiUtils.makeHomeItem(home));
        gui.addSlotAction(slot, event -> {
            if(event.isShiftClick()) {
                wGui.openNext(player, wGui.config, wGui.cache, new EditOrDeleteGui<>(home));
            } else {
                player.teleport(home.getLocation());
            }
        });
    }

    public static GuiItem makePlaceItem(Place place) {
        PaperItemBuilder paperItemBuilder = PaperItemBuilder.from(place.getItemStack())
                .name(TextUtils.stripDecoration(place.getName()).color(NamedTextColor.YELLOW));
        if(place.getBuilder() != null) {
            paperItemBuilder.lore(Component.text(place.getBuilder()));
        }
        return paperItemBuilder.asGuiItem();
    }

    public static GuiItem makeWarpItem(Warp warp) {
        return PaperItemBuilder.from(warp.getItemStack())
                .name(TextUtils.stripDecoration(warp.getName()).color(NamedTextColor.YELLOW))
                .asGuiItem();
    }

    public static GuiItem makeHomeItem(Home home) {
        return PaperItemBuilder.from(home.getItemStack())
                .name(TextUtils.stripDecoration(home.getName()).color(NamedTextColor.YELLOW))
                .asGuiItem();
    }

    public static void defaultCancel(Gui gui) {
        gui.setDefaultClickAction(event -> event.setCancelled(true));
    }

    public static ItemStack getCustomTextureHead(String value, Component name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        head.editMeta(SkullMeta.class, skullMeta -> {
            UUID uuid = UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
            PlayerProfile profile = Bukkit.createProfile(uuid, PlainTextComponentSerializer
                    .plainText()
                    .serialize(name)
                    .substring(0, 16)
                    .replace(' ', 'e'));
            profile.setProperty(new ProfileProperty("textures", value));
            skullMeta.setPlayerProfile(profile);
            skullMeta.displayName(name);
        });
        return head;
    }

}
