package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.guis.AcceptsChatGui;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.text.PlaceholderParser;
import me.ppgome.wayfinder.text.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CategoryBuilderGui extends WGui implements AcceptsChatGui {

    private Component name;
    private GuiItem guiItem = PaperItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(TextUtils.stripDecoration(Component.text("Click with item to add...")))
            .asGuiItem();
    private Category category;
    private boolean suppressCloseEvent = false;

    public CategoryBuilderGui() {
        this.name = Component.text("Click here to name...");
    }

    public CategoryBuilderGui(Category category) {
        this.name = TextUtils.parseMiniMessage(category.getName())
                .append(Component.text(" - Can't edit!", NamedTextColor.RED));
        this.guiItem = PaperItemBuilder.from(category.getItemStack()).asGuiItem();
        this.category = category;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    protected Gui buildGui(Player player) {

        boolean editing = category != null;
        Component title;
        if(editing) title = PlaceholderParser.of(config.GUI_PLACE_CAT_BUILDER).parse();
        else title = Component.text("Creating a new category...");

        gui = Gui.gui().title(title).rows(3).create();

        /*
        Set name button
         */
        setNameTag();

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
        gui.setItem(14, PaperItemBuilder.from(Material.EMERALD)
                .name(TextUtils.stripDecoration("Confirm!").color(NamedTextColor.GREEN))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    WGui hopBackTo;
                    ItemStack itemStack = gui.getGuiItem(12).getItemStack();

                    // Verify the item was changed
                    if(itemStack.getType().equals(Material.GRAY_STAINED_GLASS_PANE)) return;

                    if(editing) {
                        category.setItemStack(itemStack);
                        hopBackTo = lastGui;
                    } else {
                        cache.addCategory(new Category(mm.serialize(name), itemStack));
                        hopBackTo = this;
                    }
                    suppressCloseEvent = true;
                    hopBackTo.getLast().nullify();
                    hopBackTo.openLast();
                    suppressCloseEvent = false;
                }));

        /*
        Cancel button
         */
        gui.setItem(16, PaperItemBuilder.from(Material.BARRIER)
                .name(TextUtils.stripDecoration("Cancel").color(NamedTextColor.RED))
                .asGuiItem(event -> {
                    WGui hopBackTo;
                    if(editing) hopBackTo = lastGui;
                    else hopBackTo = this;
                    suppressCloseEvent = true;
                    hopBackTo.openLast();
                    suppressCloseEvent = false;
                }));

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

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void submitChatEntry(Component entry) {
        this.name = TextUtils.parseMiniMessage(entry);
    }

    @Override
    public void update() {
        setNameTag();
        gui.update();
    }

    private void setNameTag() {
        gui.setItem(10, PaperItemBuilder.from(Material.NAME_TAG)
                .name(name)
                .asGuiItem(event -> {
                    if(category == null) {
                        UUID uuid = player.getUniqueId();
                        cache.addEditingGui(uuid, this);
                        cache.addAwaitingChat(uuid);
                        gui.close(player);
                        player.sendMessage(PlaceholderParser.of(config.PLACE_CATEGORY_EDITING_NAME).prefix(config.PREFIX).parse());
                    }
                    event.setCancelled(true);
                }));
    }

}

