package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.*;
import me.ppgome.wayfinder.guis.AcceptsChatGui;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.text.PlaceholderParser;
import me.ppgome.wayfinder.text.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlaceBuilderGui extends WGui implements AcceptsChatGui {

    private Component name;
    private Component builder;
    private final Component defaultBuilder = Component.text("Click here to add a builder (or more)");
    private Location editingLocation;
    private Map<String, Category> categories;
    private NetherPortalLink netherPortalLink;
    private GuiItem guiItem = PaperItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(TextUtils.stripDecoration(Component.text("Icon (click with item)")))
            .asGuiItem();
    private boolean pinned;
    private long expires;
    private Place place;
    private boolean suppressCloseEvent = false;
    FieldType fieldType;
    CategorizedPlaceTable categorizedPlaceTable = Wayfinder.PLUGIN.getDatabase().getCategorizedPlaceTable();
    PlaceTable placeTable = Wayfinder.PLUGIN.getDatabase().getPlaceTable();
    NetherPortalLinkTable netherPortalLinkTable =  Wayfinder.PLUGIN.getDatabase().getNetherPortalLinkTable();

    enum FieldType {
        NAME, BUILDERS, EXPIRES
    }

    public PlaceBuilderGui() {
        this.name = Component.text("Click here to name...");
        this.builder = defaultBuilder;
        this.categories = new HashMap<>();
        this.pinned = false;
        this.expires = 0;
    }

    public PlaceBuilderGui(Place place) {
        this.place = place;
        this.name = Component.text(place.getName());
        String tempBuilder = place.getBuilder();
        if(tempBuilder != null) {
            this.builder = Component.text(place.getBuilder());
        } else {
            this.builder = null;
        }
        editingLocation = place.getLocation();
        this.categories = new HashMap<>();
        for(Category category : place.getCategories()) this.categories.put(category.getName(), category);
        this.guiItem = PaperItemBuilder.from(place.getItemStack()).asGuiItem();
        this.pinned = place.isPinned();
        this.expires = place.getExpires();
        this.netherPortalLink = place.getNetherPortalLink();
    }

    @Override
    protected Gui buildGui(Player player) {

        boolean editing = place != null;

        Component title;
        if(editing) title = PlaceholderParser.of(config.GUI_PLACE_PLACE_BUILDER).parse();
        else title = Component.text("Creating new place...");

        gui = Gui.gui().title(title).rows(5).create();

        setLocation();
        setName();
        setBuilders();
        setIcon();
        setCategories();
        setPinned();
        setExpires();
        setNetherPortalLink();
        confirmButton();
        cancelButton();

        /*
        Other standard thingies
         */
        GuiUtils.fillWhitePanesEditor(gui);
        gui.setCloseGuiAction(event -> {
            if(!suppressCloseEvent) {
                cache.addEditingGui(player.getUniqueId(), this);
            }
        });

        return gui;
    }

    @Override
    public void submitChatEntry(Component entry) {
        if(fieldType == FieldType.NAME) {
            this.name = entry;
        } else if(fieldType == FieldType.BUILDERS) {
            this.builder = entry;
        } else if(fieldType == FieldType.EXPIRES) {
            this.expires = TextUtils.stringToMilliseconds(PlainTextComponentSerializer.plainText().serialize(entry));
        }
        fieldType = null;
    }

    @Override
    public void update() {
        setName();
        setBuilders();
        setIcon();
        setCategories();
        setPinned();
        setExpires();
        setNetherPortalLink();
        gui.update();
    }

    private void setLocation() {
        if(place != null) {
            gui.setItem(4, PaperItemBuilder.from(Material.COMPASS)
                            .name(TextUtils.stripDecoration(Component.text("Update to current location")))
                            .lore(TextUtils.stripDecoration(Component.text("Current location: ", NamedTextColor.WHITE))
                                    .append(TextUtils.locationToComponent(editingLocation)))
                    .asGuiItem(event -> {
                        editingLocation = player.getLocation();
                        setLocation();
                        gui.update();
                        event.setCancelled(true);
                    }));
        }
    }

    /**
     *
     */
    private void setName() {
        gui.setItem(10, PaperItemBuilder.from(Material.NAME_TAG)
                .name(name)
                .asGuiItem(event -> {
                    UUID uuid = player.getUniqueId();
                    cache.addEditingGui(uuid, this);
                    cache.addAwaitingChat(uuid);
                    gui.close(player);
                    fieldType = FieldType.NAME;
                    player.sendMessage(PlaceholderParser.of(config.PLACE_PLACE_EDITING_NAME).prefix(config.PREFIX).parse());
                    event.setCancelled(true);
                }));
    }

    /**
     *
     */
    private void setBuilders() {
        gui.setItem(12, PaperItemBuilder.from(Material.PLAYER_HEAD)
                .name(builder != null ? builder : defaultBuilder)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    UUID uuid = player.getUniqueId();
                    cache.addEditingGui(uuid, this);
                    cache.addAwaitingChat(uuid);
                    gui.close(player);
                    fieldType = FieldType.BUILDERS;
                    player.sendMessage(PlaceholderParser.of(config.PLACE_PLACE_EDITING_BUILDERS).prefix(config.PREFIX).parse());
                }));
    }

    /**
     *
     */
    private void setIcon() {
        gui.setItem(14, guiItem);
        gui.addSlotAction(14, event -> {
            ItemStack clickedItem = event.getCursor();
            if(clickedItem.getType() != Material.AIR) {
                gui.updateItem(14, clickedItem);
            }
            event.setCancelled(true);
        });
    }

    /**
     *
     */
    protected void setCategories() {
        List<Component> catButtonLore = new ArrayList<>();
        for(Category category : categories.values()) {
            catButtonLore.add(Component.text(category.getName()));
        }
        gui.setItem(16, PaperItemBuilder.from(Material.CHEST)
                .name(Component.text("Categories"))
                .lore(catButtonLore)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    openNext(player, config, cache, new PlaceBuilderCategoryGui(name, categories));
                }));
    }

    protected void updateCategories(Map<String, Category> categoryMap) {
        this.categories = categoryMap;
        setCategories();
        if(categories.get("Nether Portals") != null && netherPortalLink == null) {
            netherPortalLink = new NetherPortalLink();
        } else if(netherPortalLink != null) {
            netherPortalLink = null;
        }
        setNetherPortalLink();
    }

    /**
     *
     */
    private void setPinned() {
        int slot = 28;
        gui.setItem(slot, buildPinnedButton());
        gui.addSlotAction(slot, event -> {
            event.setCancelled(true);
            pinned = !pinned;
            gui.updateItem(slot, buildPinnedButton());
        });

    }

    private GuiItem buildPinnedButton() {
        Material material = pinned ? Material.LIME_CANDLE : Material.RED_CANDLE;
        Component text = pinned ? Component.text("Will be pinned to main page", NamedTextColor.GREEN) :
                Component.text("Won't be pinned to main page", NamedTextColor.RED);
        return PaperItemBuilder.from(material)
                .name(text)
                .asGuiItem();
    }

    /**
     *
     */
    private void setExpires() {
        TextComponent text = Component.text("Expires: " + (expires == 0 ? "Never!" : TextUtils.millisecondsToReadableTime(expires)));
        gui.setItem(30, PaperItemBuilder.from(Material.CLOCK)
                .name(text)
                .asGuiItem(event -> {
                    if(place == null) {
                        UUID uuid = player.getUniqueId();
                        cache.addEditingGui(uuid, this);
                        cache.addAwaitingChat(uuid);
                        gui.close(player);
                        fieldType = FieldType.EXPIRES;
                        player.sendMessage(PlaceholderParser.of(config.PLACE_PLACE_EDITING_EXPIRES).prefix(config.PREFIX).parse());
                    }
                    event.setCancelled(true);
                }));
    }

    /**
     *
     */
    private void setNetherPortalLink() {
        if(netherPortalLink != null) {
            gui.setItem(40, PaperItemBuilder.from(Material.RECOVERY_COMPASS)
                    .name(TextUtils.stripDecoration("Nether Portal Pair"))
                    .lore(buildLore())
                    .asGuiItem(event -> {
                        event.setCancelled(true);
                        netherPortalLink.setPartnerLocation(player.getLocation());
                        setNetherPortalLink();
                        gui.update();
                    }));
        }
    }

    private List<Component> buildLore() {
        return List.of(
                Component.text("Click to set the location of this"),
                Component.text("Nether portal on the nether side."),
                Component.text("You can close this menu and re-open"),
                Component.text("it once in position."),
                TextUtils.stripDecoration(Component.text("Current location: ", NamedTextColor.WHITE))
                        .append(netherPortalLink.getPartnerLocation() != null
                                ? TextUtils.locationToComponent(netherPortalLink.getPartnerLocation())
                                : Component.text("Location unset"))
        );
    }

    /**
     *
     */
    private void confirmButton() {
        gui.setItem(32, PaperItemBuilder.from(Material.EMERALD)
                .name(TextUtils.stripDecoration("Confirm!").color(NamedTextColor.GREEN))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    WGui hopBackTo;
                    ItemStack itemStack = gui.getGuiItem(14).getItemStack();

                    // Verify the item was changed and that the place was assigned at least one category
                    if(itemStack.getType().equals(Material.GRAY_STAINED_GLASS_PANE) || categories.isEmpty()) return;

                    Place checkPlace = cache.getPlace(mm.serialize(name));
                    if(checkPlace != null && (place == null || checkPlace != place)) {
                            player.sendMessage(PlaceholderParser.of(config.PLACE_PLACE_ALREADY_EXISTS).prefix(config.PREFIX).parse());
                            return;
                    }

                    Place tempPlace;

                    if(place != null) {
                        String oldName = place.getName();
                        Set<Category> oldCategories = new HashSet<>(place.getCategories());
                        place.setName(mm.serialize(name));
                        place.setItemStack(itemStack);
                        place.setLocation(editingLocation);
                        if(builder != null) place.setBuilder(mm.serialize(builder));
                        place.setPinned(pinned);
                        place.setCategories(categories);
                        reorganizeCategories(place, oldName, oldCategories, new HashSet<>(categories.values()));
                        tempPlace = place;
                        hopBackTo = lastGui;
                    } else {
                        Place newPlace = new Place(mm.serialize(name), player.getLocation(),
                                builder.equals(defaultBuilder) ? null : mm.serialize(builder), itemStack, pinned, categories);
                        cache.addPlace(newPlace);
                        for(Category category : categories.values()) {
                            category.addPlace(newPlace);
                            categorizedPlaceTable.save(new CategorizedPlace(category.getName(), newPlace.getName()));
                        }
                        if(expires != 0) newPlace.setExpires(expires);
                        tempPlace = newPlace;
                        hopBackTo = this;
                    }

                    placeTable.save(tempPlace);
                    if(netherPortalLink != null) {
                        netherPortalLink.setPlaceName(tempPlace.getName());
                        netherPortalLinkTable.save(netherPortalLink);
                        tempPlace.setNetherPortalLink(netherPortalLink);
                        cache.addNetherPortalPlace(tempPlace);
                    } else {
                        netherPortalLinkTable.delete(new NetherPortalLink(tempPlace.getName()));
                        tempPlace.setNetherPortalLink(null);
                        cache.removeNetherPortalPlace(tempPlace);
                    }

                    if(this.categories.get("End Portals") != null) {
                        cache.addEndPortalPlace(tempPlace);
                    } else {
                        cache.removeEndPortalPlace(tempPlace);
                    }

                    if(tempPlace.isPinned()) cache.addPinnedPlace(tempPlace);
                    if(!tempPlace.isPinned() && cache.getPinnedPlace(tempPlace.getName()) != null) cache.removePinnedPlace(tempPlace.getName());
                    suppressCloseEvent = true;
                    hopBackTo.getLast().nullify();
                    hopBackTo.openLast();
                    suppressCloseEvent = false;
                }));
    }

    /**
     *
     */

    /**
     * Sync category changes for an edited place, both in the database (categorized_place table)
     * and in-memory (each Category's Set<Place>), using the correct old/new place name.
     *
     * @param place the place being edited (already has its NEW name/categories set on it)
     * @param oldPlaceName the place's name BEFORE this edit (used to clear old DB rows)
     * @param oldCategories the place's categories BEFORE this edit (snapshot taken before setCategories() ran)
     * @param newCategories the place's categories AFTER this edit
     */
    private void reorganizeCategories(Place place, String oldPlaceName, Set<Category> oldCategories, Set<Category> newCategories) {
        Set<Category> removed = new HashSet<>(oldCategories);
        removed.removeAll(newCategories);
        Set<Category> added = new HashSet<>(newCategories);
        added.removeAll(oldCategories);

        for(Category category : removed) {
            category.removePlace(place);
            categorizedPlaceTable.delete(oldPlaceName, category.getName());
        }

        for(Category category : added) {
            category.addPlace(place);
            categorizedPlaceTable.save(new CategorizedPlace(category.getName(), place.getName()));
        }
    }

    /**
     *
     */
    private void cancelButton() {
        gui.setItem(34, PaperItemBuilder.from(Material.BARRIER)
                .name(TextUtils.stripDecoration("Cancel").color(NamedTextColor.RED))
                .asGuiItem(event -> {
                    WGui hopBackTo;
                    if(place != null) hopBackTo = lastGui;
                    else hopBackTo = this;
                    suppressCloseEvent = true;
                    hopBackTo.openLast();
                    suppressCloseEvent = false;
                }));
    }
}
