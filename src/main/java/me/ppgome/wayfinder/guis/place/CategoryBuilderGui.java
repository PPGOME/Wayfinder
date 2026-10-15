package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.guis.AcceptsChatGui;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.text.PlaceholderParser;
import me.ppgome.wayfinder.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategoryBuilderGui extends WGui implements AcceptsChatGui {

    private Component name;
    private int priority = 0;
    private String iconUrl;
    private GuiItem guiItem = PaperItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(TextUtils.stripDecoration(Component.text("Click with item to add...")))
            .asGuiItem();
    private Category category;
    private boolean suppressCloseEvent = false;
    private FieldType fieldType;

    private static final List<Component> PRIORITY_LORE = List.of(
            Component.text("When BlueMap chooses which icon to display for"),
            Component.text("a place, higher priority always wins."),
            Component.text("Defaults to 0.")
    );
    private static final List<Component> ICON_LORE = List.of(
            Component.text("Must be a direct image URL."),
            Component.text("This is an optional field.")
    );

    public CategoryBuilderGui() {
        this.name = Component.text("Click here to name...");
    }

    public CategoryBuilderGui(Category category) {
        this.name = TextUtils.parseMiniMessage(category.getName());
        this.guiItem = PaperItemBuilder.from(category.getItemStack()).asGuiItem();
        this.category = category;
        this.priority = category.getPriority();
        this.iconUrl = category.getBluemapIcon();
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    protected Gui buildGui(Player player) {

        boolean editing = category != null;
        Component title;
        if(editing) title = PlaceholderParser.of(config.GUI_PLACE_CAT_BUILDER).parse();
        else title = Component.text("Creating a new category...");

        gui = Gui.gui().title(title).rows(5).create();

        /*
        Set data buttons
         */
        setNameTag();
        setPriority();
        setBlueMapIcon();

        /*
        Set icon button
         */
        gui.setItem(12, guiItem);

        // Click event
        gui.addSlotAction(12, event -> {
            ItemStack clickedItem = event.getCursor();
            if(clickedItem.getType() != Material.AIR) {
                gui.updateItem(12, clickedItem);
            }
            event.setCancelled(true);
        });

        /*
        Confirm button
         */
        gui.setItem(30, PaperItemBuilder.from(Material.EMERALD)
                .name(TextUtils.stripDecoration("Confirm!").color(NamedTextColor.GREEN))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    WGui hopBackTo;
                    ItemStack itemStack = gui.getGuiItem(12).getItemStack();

                    // Verify the item was changed
                    if(itemStack.getType().equals(Material.GRAY_STAINED_GLASS_PANE)) return;
                    String stringifiedName = PlainTextComponentSerializer.plainText().serialize(name);
                    Category checkCat = cache.getCategory(stringifiedName);
                    if(checkCat != null && (category == null || checkCat != category)) {
                        player.sendMessage(PlaceholderParser.of(config.PLACE_CATEGORY_ALREADY_EXISTS).prefix(config.PREFIX).parse());
                        return;
                    }

                    if(editing) {
                        category.setName(stringifiedName);
                        category.setPriority(priority);
                        category.setBluemapIcon(iconUrl);
                        category.setItemStack(itemStack);
                        cache.updateCategory(category);
                        hopBackTo = lastGui;
                    } else {
                        cache.addCategory(new Category(stringifiedName, priority, itemStack));
                        hopBackTo = this;
                    }
                    suppressCloseEvent = true;
                    hopBackTo.getLast().nullify();
                    hopBackTo.openLast();
                    suppressCloseEvent = false;
                    cache.removeEditingGui(player.getUniqueId());
                }));

        /*
        Cancel button
         */
        gui.setItem(32, PaperItemBuilder.from(Material.BARRIER)
                .name(TextUtils.stripDecoration("Cancel").color(NamedTextColor.RED))
                .asGuiItem(event -> {
                    WGui hopBackTo;
                    if(editing) hopBackTo = lastGui;
                    else hopBackTo = this;
                    cache.removeEditingGui(player.getUniqueId());
                    suppressCloseEvent = true;
                    hopBackTo.openLast();
                    suppressCloseEvent = false;
                }));

        /*
        Other standard thingies
         */
        GuiUtils.fillWhitePanes(gui);
        gui.setCloseGuiAction(event -> {
            if(!suppressCloseEvent) {
                cache.addEditingGui(player.getUniqueId(), this);
            }
        });

        return gui;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void submitChatEntry(Component entry) {
        if(fieldType == FieldType.NAME) {
            this.name = TextUtils.parseMiniMessage(entry);
        } else if(fieldType == FieldType.PRIORITY) {
            String input = PlainTextComponentSerializer.plainText().serialize(entry);
            if(isInteger(input)) {
                this.priority = Integer.parseInt(input);
            } else priority = 0;
        }  else if(fieldType == FieldType.ICON) {
            this.iconUrl = PlainTextComponentSerializer.plainText().serialize(entry);
        }
    }

    @Override
    public void update() {
        setNameTag();
        setBlueMapIcon();
        setPriority();
        gui.update();
    }

    private void setNameTag() {
        gui.setItem(10, chatFieldButton(PaperItemBuilder.from(Material.NAME_TAG).name(name),
                FieldType.NAME, config.PLACE_CATEGORY_EDITING_NAME));
    }

    private void setPriority() {
        List<Component> loreCopy =  new ArrayList<>(PRIORITY_LORE);
        loreCopy.add(Component.text("Priority: " + priority,  NamedTextColor.GREEN));
        gui.setItem(14, PaperItemBuilder.from(Material.GOLD_INGOT)
                .name(TextUtils.stripDecoration("Priority").color(NamedTextColor.GREEN))
                .lore(loreCopy)
                .asGuiItem(event -> {
                    UUID uuid = player.getUniqueId();
                    cache.addEditingGui(uuid, this);
                    cache.addAwaitingChat(uuid);
                    fieldType = FieldType.PRIORITY;
                    gui.close(player);
                    player.sendMessage(PlaceholderParser.of(config.PLACE_CATEGORY_EDITING_PRIORITY).prefix(config.PREFIX).parse());
                    event.setCancelled(true);
                }));
    }

    private void setBlueMapIcon() {

        List<Component> loreCopy =  new ArrayList<>(ICON_LORE);
        if(iconUrl != null) loreCopy.add(Component.text(iconUrl, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));

        gui.setItem(16, chatFieldButton(
                PaperItemBuilder.from(Material.FILLED_MAP)
                        .name(TextUtils.stripDecoration("BlueMap Icon").color(NamedTextColor.GREEN))
                        .lore(loreCopy),
                FieldType.ICON,
                config.PLACE_CATEGORY_EDITING_ICON
        ));
    }

    private GuiItem chatFieldButton(PaperItemBuilder builder, FieldType type, String message) {
        return builder.asGuiItem(event -> {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            cache.addEditingGui(uuid, this);
            cache.addAwaitingChat(uuid);
            fieldType = type;
            gui.close(player);
            player.sendMessage(PlaceholderParser.of(message).prefix(config.PREFIX).parse());
        });
    }

    //------------------------------------------------------------------------------------------------------------------

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}

