package me.ppgome.wayfinder.guis;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.Home;
import me.ppgome.wayfinder.database.Warp;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.navigation.ParticleNavigator;
import me.ppgome.wayfinder.text.PlaceholderParser;
import me.ppgome.wayfinder.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GuiUtils {

    // =================================================================================================================
    // Layout / Slots
    // =================================================================================================================

    /**
     * Get the slot placement for centering items in a GUI area.
     * @param itemCount the number of items to place
     * @param width the width of the area
     * @param height the height of the area
     * @param startSlot the starting slot of the area
     * @return an array of slot indices where the items should be placed
     */
    public static int[] getSlotPlacement(int itemCount, int width, int height, int startSlot) {

        if (itemCount > width * height)
            throw new IllegalArgumentException("Too many items for this region");

        int startRow = startSlot / 9;
        int startCol = startSlot % 9;

        int[] result = new int[itemCount];
        int idx = 0;

        for (int r = 0; r < height && idx < itemCount; r++) {
            int itemsInRow = Math.min(width, itemCount - idx);
            int leftPad = (width - itemsInRow) / 2;

            for (int c = 0; c < itemsInRow; c++) {
                int row = startRow + r;
                int col = startCol + leftPad + c;
                result[idx++] = row * 9 + col;
            }
        }

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

//    /**
//     * Insert gray panes into a GUI between two slots (inclusive).
//     * @param gui the GUI to insert panes into
//     * @param firstSlot the first slot to insert a pane into
//     * @param lastSlot the last slot to insert a pane into
//     */
//    public static void insertGrayPanes(Gui gui, int firstSlot, int lastSlot) {
//
//        GuiItem pane = PaperItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem();
//
//        for(int i = firstSlot; i <= lastSlot; i++) {
//            if(isBorder(i)) continue;
//            gui.setItem(i, pane);
//        }
//    }

    public static boolean isBorder(int slot) {
        int posInRow = slot % 9;
        return posInRow == 8 || posInRow == 0;
    }

    /**
     * All non-border slots between two slots (inclusive), in order.
     * This is the one place the "skip the border columns" rule lives.
     */

    public static List<Integer> slotsBetween(int firstSlot, int lastSlot) {
        List<Integer> slots = new ArrayList<>();
        for(int i = firstSlot; i <= lastSlot; i++) {
            if(!isBorder(i)) slots.add(i);
        }
        return  slots;
    }

    /**
     * Every usable slot inside the GUI's border: skips the first row, the last row and the two edge columns.
     * A 6-row GUI gives slots 10-43 (28 slots); a 4-row GUI gives 10-25 (14 slots).
     */

    public static List<Integer> interiorSlots(BaseGui gui) {
        return slotsBetween(9, (gui.getRows() - 1) * 9 - 1);
    }

    /**
     * Hand each item to placer along with the next free slot. Stops quietly when the slots run out,
     * so a long list can no longer spill over borders or onto other buttons.
     */
    public static <T> void placeAll(Iterable<T> items, List<Integer> slots, BiConsumer<Integer, T> placer) {
        Iterator<Integer> slotIterator = slots.iterator();
        for (T item : items) {
            if (!slotIterator.hasNext()) return;
            placer.accept(slotIterator.next(), item);
        }
    }

    // =================================================================================================================
    // Panes
    // =================================================================================================================

    /**
     * A blank, click-proof pane.
     */
    public static GuiItem pane(Material material) {
        return PaperItemBuilder.from(material)
                .name(Component.text(" "))
                .asGuiItem(event -> event.setCancelled(true));
    }

    /**
     * Insert gray panes into a GUI between two slots (inclusive), skipping border columns.
     */
    public static void insertGrayPanes(BaseGui gui, int firstSlot, int lastSlot) {
        GuiItem pane = pane(Material.GRAY_STAINED_GLASS_PANE);
        for (int slot : slotsBetween(firstSlot, lastSlot)) {
            gui.setItem(slot, pane);
        }
    }

    /**
     * Insert gray panes into every interior slot of the GUI.
     */
    public static void insertGrayPanes(BaseGui gui) {
        GuiItem pane = pane(Material.GRAY_STAINED_GLASS_PANE);
        for (int slot : interiorSlots(gui)) {
            gui.setItem(slot, pane);
        }
    }

    /**
     * Fill all remaining empty slots with click-proof white panes.
     */
    public static void fillWhitePanes(BaseGui gui) {
        gui.getFiller().fill(pane(Material.WHITE_STAINED_GLASS_PANE));
    }

    public static void defaultCancel(BaseGui gui) {
        gui.setDefaultClickAction(event -> event.setCancelled(true));
    }

    // =================================================================================================================
    // Buttons
    // =================================================================================================================

    /**
     * A named, coloured, non-italic button that always cancels the click before running {@code action}.
     */
    public static GuiItem button(Material material, String name, NamedTextColor colour,
                                 Consumer<InventoryClickEvent> action) {
        return PaperItemBuilder.from(material)
                .name(TextUtils.stripDecoration(name).color(colour))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    action.accept(event);
                });
    }

    /**
     * The item for confirming in a GUI. Used mainly for editors
     * @param action the action to run on click
     * @return A GuiItem for placing in a GUI
     */
    public static GuiItem confirm(Consumer<InventoryClickEvent> action) {
        return button(Material.EMERALD, "Confirm!", NamedTextColor.GREEN, action);
    }

    /**
     * The item for cancelling in a GUI. Used mainly for editors
     * @param action the action to run on click
     * @return A GuiItem for placing in a GUI
     */
    public static GuiItem cancel(Consumer<InventoryClickEvent> action) {
        return button(Material.BARRIER, "Cancel", NamedTextColor.RED, action);
    }

    /**
     * An on/off settings button. The name ("Enable x" / "Disable x") and its colour come from {@code enabled};
     * the click is cancelled and then handed to {@code onToggle}, which should flip the value and rebuild the button.
     * @param noun what is being toggled, e.g. "particle guide"
     * @param icon a fresh ItemStack to use as the icon (its display name is overwritten)
     * @param lore plain lore lines
     */
    public static GuiItem toggleButton(String noun, boolean enabled, ItemStack icon, List<String> lore,
                                       Consumer<InventoryClickEvent> onToggle) {
        icon.editMeta(meta -> meta.displayName(
                TextUtils.stripDecoration((enabled ? "Disable " : "Enable ") + noun)
                        .color(enabled ? NamedTextColor.RED : NamedTextColor.GREEN)));

        List<Component> loreLines = lore.stream()
                .map(TextUtils::stripDecoration)
                .toList();

        return PaperItemBuilder.from(icon)
                .lore(loreLines)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    onToggle.accept(event);
                });
    }

    // =================================================================================================================
    // Place / Warp / Home Entries
    // =================================================================================================================

    /**
     * Shared wiring for place, warp and home buttons: shift-click opens the edit/delete screen (when allowed),
     * anything else runs {@code onUse}.
     * If EditOrDeleteGui has a bound on its type parameter, put the same bound on T here.
     */
    private static <T> void addEntryItem(WGui wGui, BaseGui gui, int slot, GuiItem item, T entry, Player player,
                                         boolean canEdit, Consumer<InventoryClickEvent> onUse) {
        gui.setItem(slot, item);
        gui.addSlotAction(slot, event -> {
            if (event.isShiftClick() && canEdit) {
                wGui.openNext(player, wGui.config, wGui.cache, new EditOrDeleteGui<>(entry));
            } else {
                onUse.accept(event);
            }
        });
    }

    /**
     * A place button that carries its own click action, so it works with addItem() / pagination.
     * @param wGui the GUI being added to
     * @param place the place whose item is being added
     * @param player the player viewing the GUI
     * @return an item representing a place
     */
    public static GuiItem placeItem(WGui wGui, Place place, Player player) {
        boolean canEdit = player.hasPermission("wayfinder.admin.place");
        GuiItem item = makePlaceItem(place, player);

        item.setAction(event -> {
            event.setCancelled(true);
            if (event.isShiftClick() && canEdit) {
                wGui.openNext(player, wGui.config, wGui.cache, new EditOrDeleteGui<>(place));
                return;
            }
            if (wGui.cache.isRouting(player.getUniqueId())) {
                player.sendMessage(PlaceholderParser.of(wGui.config.PLACE_ALREADY_ROUTING).place(place.getName()).parse());
            } else {
                new ParticleNavigator(Wayfinder.PLUGIN, player, place).startDisplaying();
            }
            wGui.gui.close(player);
        });
        return item;
    }

    /**
     * Adds a button to a GUI for teleporting to/editing a place
     * @param wGui The GUI directory
     * @param gui The GUI being edited
     * @param slot The slot the item is being added to
     * @param place The place being routed to/edited
     * @param player The player routing to/editing the place
     */
    public static void addPlaceItem(WGui wGui, BaseGui gui, int slot, Place place, Player player) {
        boolean canEdit = player.hasPermission("wayfinder.admin.place");
        addEntryItem(wGui, gui, slot, makePlaceItem(place, player), place, player, canEdit, event -> {
            if (wGui.cache.isRouting(player.getUniqueId())) {
                player.sendMessage(PlaceholderParser.of(wGui.config.PLACE_ALREADY_ROUTING).place(place.getName()).parse());
            } else {
                new ParticleNavigator(Wayfinder.PLUGIN, player, place).startDisplaying();
            }
            wGui.gui.close(player);
        });
    }

    /**
     * Adds a button to a GUI for teleporting to/editing a warp
     * @param wGui The GUI directory
     * @param gui The GUI being edited
     * @param slot The slot the item is being added to
     * @param warp The warp being warped to/edited
     * @param player The player warping to/editing the warp
     */
    public static void addWarpItem(WGui wGui, BaseGui gui, int slot, Warp warp, Player player) {
        addEntryItem(wGui, gui, slot, makeWarpItem(warp), warp, player, true, event -> {
            player.teleport(warp.getLocation());
            player.sendMessage(PlaceholderParser.of(wGui.config.WARP_WARP_SUCCESS).prefix(wGui.config.PREFIX).parse());
        });
    }

    /**
     * Adds a button to a GUI for teleporting to/editing a home
     * @param wGui The GUI directory
     * @param gui The GUI being edited
     * @param slot The slot the item is being added to
     * @param home The home being warped to/edited
     * @param player The player warping to/editing the home
     */
    public static void addHomeItem(WGui wGui, BaseGui gui, int slot, Home home, Player player) {
        addEntryItem(wGui, gui, slot, makeHomeItem(home), home, player, true, event -> {
            if(event.isShiftClick()) {
                wGui.openNext(player, wGui.config, wGui.cache, new EditOrDeleteGui<>(home));
            } else {
                player.teleport(home.getLocation());
            }
        });
    }

    /**
     * The icon + yellow, non-italic name that places, warps and homes all share.
     */
    private static PaperItemBuilder namedEntry(ItemStack icon, String name) {
        return PaperItemBuilder.from(icon)
                .name(TextUtils.stripDecoration(name).color(NamedTextColor.YELLOW));
    }

    private static Component noItalic(Component component) {
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static GuiItem makePlaceItem(Place place, Player player) {
        Location placeLocation = place.getLocation();
        List<Component> loreList = new ArrayList<>();

        if (place.getBuilder() != null) {
            loreList.add(noItalic(Component.text(place.getBuilder())));
        }
        loreList.add(noItalic(TextUtils.locationToComponent(placeLocation)));

        String distanceText = player.getLocation().getWorld() == placeLocation.getWorld()
                ? String.format(Locale.ROOT, "%.2f", player.getLocation().distance(placeLocation)) + " blocks away"
                : "Hm... that seems out of this world!";
        loreList.add(noItalic(Component.text(distanceText, NamedTextColor.GOLD)));

        return namedEntry(place.getItemStack(), place.getName())
                .lore(loreList)
                .asGuiItem();
    }


    public static GuiItem makeWarpItem(Warp warp) {
        return namedEntry(warp.getItemStack(), warp.getName()).asGuiItem();
    }


    public static GuiItem makeHomeItem(Home home) {
        return namedEntry(home.getItemStack(), home.getName()).asGuiItem();
    }

    // =================================================================================================================
    // Heads
    // =================================================================================================================

    /**
     * Build a player head with a custom texture. This hits the profile API every call, so build it once and
     * {@code clone()} it if you show it often.
     */
    public static ItemStack getCustomTextureHead(String value, Component name) {
        String plainName = PlainTextComponentSerializer.plainText().serialize(name);
        String profileName = plainName.substring(0, Math.min(plainName.length(), 16)).replace(' ', 'e');
        UUID uuid = UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        head.editMeta(SkullMeta.class, skullMeta -> {
            PlayerProfile profile = Bukkit.createProfile(uuid, profileName);
            profile.setProperty(new ProfileProperty("textures", value));
            skullMeta.setPlayerProfile(profile);
            skullMeta.displayName(name);
        });
        return head;
    }


}
