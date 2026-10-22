package me.ppgome.wayfinder.guis.place.editors;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.*;
import me.ppgome.wayfinder.guis.EditorGui;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.hooks.BlueMapHook;
import me.ppgome.wayfinder.text.PlaceholderParser;
import me.ppgome.wayfinder.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlaceBuilderGui extends EditorGui {

    private static final Component DEFAULT_NAME = Component.text("Click here to name...");
    private static final Component DEFAULT_BUILDER = Component.text("Click here to add a builder (or more)");
    private static final String NETHER_PORTALS = "Nether Portals";
    private static final String END_PORTALS = "End Portals";
    private static final List<Component> ICON_LORE = List.of(
            Component.text("Must be a direct image URL."),
            Component.text("This is an optional field."),
            Component.text("This will always take priority"),
            Component.text("over category icons.")
    );

    // Modifiable data
    private Place place;
    private Component name;
    private Component builder;
    private Location editingLocation;
    private Map<String, Category> categories;
    private NetherPortalLink netherPortalLink;
    private String bluemapIcon;
    private GuiItem guiItem = PaperItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(TextUtils.stripDecoration(Component.text("Icon (click with item)")))
            .asGuiItem();
    private boolean pinned;
    private long expires;

    private final CategorizedPlaceTable categorizedPlaceTable = Wayfinder.PLUGIN.getDatabase().getCategorizedPlaceTable();
    private final PlaceTable placeTable = Wayfinder.PLUGIN.getDatabase().getPlaceTable();
    private final NetherPortalLinkTable netherPortalLinkTable =  Wayfinder.PLUGIN.getDatabase().getNetherPortalLinkTable();
    private final BlueMapHook blueMapHook = Wayfinder.PLUGIN.getBlueMapHook();

    private boolean editing;

    public PlaceBuilderGui() {
        this.name = DEFAULT_NAME;
        this.builder = DEFAULT_BUILDER;
        this.categories = new HashMap<>();
        this.pinned = false;
        this.expires = 0;
        this.bluemapIcon = null;
        editing = false;
    }

    public PlaceBuilderGui(Place place) {
        this.place = place;
        this.name = mm.deserialize(place.getName());
        this.builder = place.getBuilder() != null ? mm.deserialize(place.getBuilder()) : null;
        editingLocation = place.getLocation();
        this.categories = new HashMap<>();
        for(Category category : place.getCategories()) this.categories.put(category.getName(), category);
        this.guiItem = PaperItemBuilder.from(place.getItemStack()).asGuiItem();
        this.bluemapIcon = place.getBluemapIcon();
        this.pinned = place.isPinned();
        this.expires = place.getExpires();
        this.netherPortalLink = place.getNetherPortalLink();
        editing = true;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    protected BaseGui buildGui(Player player) {

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
        setBluemapIcon();
        setPinned();
        setExpires();
        setNetherPortalLink();

        gui.setItem(32, GuiUtils.confirm(event -> confirm()));
        gui.setItem(34, GuiUtils.cancel(event -> leave(editing, false)));

        GuiUtils.fillWhitePanes(gui);
        installEditorCloseAction();

        return gui;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void submitChatEntry(Component entry) {
        Wayfinder.PLUGIN.getLogger().info("submitChatEntry type=" + type + " entry=" + plain(entry));
        if(type == FieldType.NAME) {
            this.name = entry;
        } else if(type == FieldType.BUILDERS) {
            this.builder = entry;
        } else if(type == FieldType.EXPIRES) {
            this.expires = TextUtils.stringToMilliseconds(plain(entry));
        } else if(type == FieldType.ICON) {
            this.bluemapIcon = plain(entry);
            Wayfinder.PLUGIN.getLogger().info("bluemapIcon set to " + bluemapIcon);
        }
        type = null;
    }

    @Override
    public void update() {
        setName();
        setBuilders();
        setIcon();
        setCategories();
        setBluemapIcon();
        setPinned();
        setExpires();
        setNetherPortalLink();
        gui.update();
    }

    //------------------------------------------------------------------------------------------------------------------

    private void setLocation() {
        if(place != null) {
            gui.setItem(4, PaperItemBuilder.from(Material.COMPASS)
                            .name(TextUtils.stripDecoration(Component.text("Update to current location")))
                            .lore(TextUtils.stripDecoration(Component.text("Current location: ", NamedTextColor.WHITE))
                                    .append(TextUtils.locationToComponent(editingLocation)))
                    .asGuiItem(event -> {
                        event.setCancelled(true);
                        editingLocation = player.getLocation();
                        setLocation();
                        gui.update();
                    }));
        }
    }

    /**
     *
     */
    private void setName() {
        gui.setItem(10, chatButton(PaperItemBuilder.from(Material.NAME_TAG).name(name),
                FieldType.NAME, config.PLACE_PLACE_EDITING_NAME));
    }

    /**
     *
     */
    private void setBuilders() {
        gui.setItem(12, chatButton(
                PaperItemBuilder.from(Material.PLAYER_HEAD).name(builder != null ? builder : DEFAULT_BUILDER),
                FieldType.BUILDERS, config.PLACE_PLACE_EDITING_BUILDERS
        ));
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
        
        boolean wantsPortalLink = categories.containsKey(NETHER_PORTALS);
        if(wantsPortalLink && netherPortalLink == null) {
            netherPortalLink = new NetherPortalLink();
        } else if(!wantsPortalLink) {
            netherPortalLink = null;
        }
        setNetherPortalLink();
    }
    
    private void setBluemapIcon() {
        List<Component> loreCopy =  new ArrayList<>(ICON_LORE);
        if(bluemapIcon != null) loreCopy.add(Component.text(bluemapIcon, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        gui.setItem(22, chatButton(
                PaperItemBuilder.from(Material.FILLED_MAP)
                        .name(Component.text("Set BlueMap icon"))
                        .lore(loreCopy),
                FieldType.ICON,
                config.PLACE_CATEGORY_EDITING_ICON
        ));
    }


    private void setPinned() {
        gui.setItem(28, pinnedButton());
    }

    /**
     *
     */
    private GuiItem pinnedButton() {
        Material material = pinned ? Material.LIME_CANDLE : Material.RED_CANDLE;
        Component text = pinned
                ? Component.text("Will be pinned to main page", NamedTextColor.GREEN)
                : Component.text("Won't be pinned to main page", NamedTextColor.RED);
        return PaperItemBuilder.from(material)
                .name(text)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    pinned = !pinned;
                    gui.updateItem(28, pinnedButton());
                });
    }


    /**
     *
     */
    private void setExpires() {
        Component text = Component.text("Expires: " + (expires == 0 ? "Never!" : TextUtils.millisecondsToReadableTime(expires)));
        gui.setItem(30, PaperItemBuilder.from(Material.CLOCK)
                .name(text)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    promptChat(FieldType.EXPIRES, config.PLACE_PLACE_EDITING_EXPIRES);
                }));
    }


    /**
     *
     */
    private void setNetherPortalLink() {
        if (netherPortalLink == null) {
            gui.setItem(40, GuiUtils.pane(Material.WHITE_STAINED_GLASS_PANE));
            return;
        }
        gui.setItem(40, PaperItemBuilder.from(Material.RECOVERY_COMPASS)
                .name(TextUtils.stripDecoration("Nether Portal Pair"))
                .lore(portalLore())
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    netherPortalLink.setPartnerLocation(player.getLocation());
                    setNetherPortalLink();
                    gui.update();
                }));
    }


    private List<Component> portalLore() {
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

    //------------------------------------------------------------------------------------------------------------------

    /**
     *
     */
    private void confirm() {
        ItemStack icon = gui.getGuiItem(14).getItemStack();

        if (icon.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            reject("Click the icon slot with an item first.");
            return;
        }
        if (categories.isEmpty() && !pinned) {
            reject("Pick at least one category or pin the place first.");
            return;
        }
        if (name.equals(DEFAULT_NAME)) {
            reject("Give the place a name first.");
            return;
        }

        String placeName = mm.serialize(name);
        Place existing = cache.getPlace(placeName);
        // "nearest" is reserved
        if (placeName.equalsIgnoreCase("nearest") || (existing != null && existing != place)) {
            player.sendMessage(PlaceholderParser.of(config.PLACE_PLACE_ALREADY_EXISTS).prefix(config.PREFIX).parse());
            return;
        }

        Place saved = editing ? applyEdits(placeName, icon) : createPlace(placeName, icon);

        placeTable.save(saved);
        syncNetherPortal(saved);
        cacheEndPortal(saved);
        cachePinned(saved);

        leave(editing, true);
    }

    /**
     *
     * @param newName
     * @param icon
     * @return
     */
    private Place applyEdits(String newName, ItemStack icon) {
        String oldName = place.getName();
        Location oldLocation = place.getLocation();
        Set<Category> oldCategories = new HashSet<>(place.getCategories());

        place.setName(newName);
        place.setItemStack(icon);
        place.setLocation(editingLocation);
        String builderText = builderText();
        if (builderText != null) place.setBuilder(builderText);
        place.setPinned(pinned);
        place.setExpires(expires);
        if(expires != 0) cache.addExpiringPlace(place);
        place.setCategories(categories);
        place.setBluemapIcon(bluemapIcon);
        reorganizeCategories(place, oldName, oldCategories, new HashSet<>(categories.values()));

        blueMapHook.removePlaceMarker(oldLocation, oldName);
        blueMapHook.addPlaceMarker(place);

        return place;
    }

    /**
     *
     * @return
     */
    private String builderText() {
        if (builder == null || builder.equals(DEFAULT_BUILDER)) return null;
        return mm.serialize(builder);
    }

    /**
     *
     * @param newName
     * @param icon
     * @return
     */
    private Place createPlace(String newName, ItemStack icon) {
        Place newPlace = new Place(newName, player.getLocation(), builderText(), icon, pinned, categories, bluemapIcon);
        cache.addPlace(newPlace);
        for (Category category : categories.values()) {
            category.addPlace(newPlace);
            categorizedPlaceTable.save(new CategorizedPlace(category.getName(), newPlace.getName()));
        }
        if (expires != 0) {
            newPlace.setExpires(expires);
            cache.addExpiringPlace(newPlace);
        }

        blueMapHook.addPlaceMarker(newPlace);

        return newPlace;
    }

    //------------------------------------------------------------------------------------------------------------------

    private void syncNetherPortal(Place saved) {
        if (netherPortalLink != null) {
            netherPortalLink.setPlaceName(saved.getName());
            netherPortalLinkTable.save(netherPortalLink);
            saved.setNetherPortalLink(netherPortalLink);
            cache.addNetherPortalPlace(saved);
        } else {
            netherPortalLinkTable.delete(new NetherPortalLink(saved.getName()));
            saved.setNetherPortalLink(null);
            cache.removeNetherPortalPlace(saved);
        }
    }

    private void cacheEndPortal(Place saved) {
        if (categories.containsKey(END_PORTALS)) {
            cache.addEndPortalPlace(saved);
        } else {
            cache.removeEndPortalPlace(saved);
        }
    }

    private void cachePinned(Place saved) {
        if (saved.isPinned()) {
            cache.addPinnedPlace(saved);
        } else if (cache.getPinnedPlace(saved.getName()) != null) {
            cache.removePinnedPlace(saved.getName());
        }
    }

    private void reorganizeCategories(Place place, String oldPlaceName, Set<Category> oldCategories, Set<Category> newCategories) {
        Set<Category> removed = new HashSet<>(oldCategories);
        removed.removeAll(newCategories);
        Set<Category> added = new HashSet<>(newCategories);
        added.removeAll(oldCategories);

        if (!oldPlaceName.equals(place.getName())) {
            categorizedPlaceTable.renamePlace(oldPlaceName, place.getName());
        }

        for(Category category : removed) {
            category.removePlace(place);
            categorizedPlaceTable.delete(oldPlaceName, category.getName());
        }

        for(Category category : added) {
            category.addPlace(place);
            categorizedPlaceTable.save(new CategorizedPlace(category.getName(), place.getName()));
        }
    }
}
