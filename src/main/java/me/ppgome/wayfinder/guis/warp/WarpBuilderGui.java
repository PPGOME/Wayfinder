package me.ppgome.wayfinder.guis.warp;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.Warp;
import me.ppgome.wayfinder.database.WarpTable;
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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class WarpBuilderGui extends WGui implements AcceptsChatGui {

    private Component name;
    private Component builder;
    private Component permission;
    private GuiItem guiItem = PaperItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(TextUtils.stripDecoration(Component.text("Icon (click with item)")))
            .asGuiItem();
    private Warp warp;
    private boolean suppressCloseEvent = false;
    FieldType fieldType;
    WarpTable warpTable = Wayfinder.PLUGIN.getDatabase().getWarpTable();

    enum FieldType {
        NAME, BUILDERS, PERMISSION
    }

    public WarpBuilderGui() {
        this.name = Component.text("Click here to name...");
        this.builder = Component.text("Click here to add a builder (or more)");
        this.permission = Component.text("Click here to add a permission (or more)");
    }

    public WarpBuilderGui(Warp warp) {
        this.warp = warp;
        this.name = Component.text(warp.getName());
        this.builder = Component.text(warp.getBuilder());
        this.guiItem = PaperItemBuilder.from(warp.getItemStack()).asGuiItem();
    }

    @Override
    protected Gui buildGui(Player player) {

        boolean editing = warp != null;

        Component title;
        if(editing) title = PlaceholderParser.of(config.GUI_WARP_BUILDER).parse();
        else title = Component.text("Creating new warp...");

        gui = Gui.gui().title(title).rows(5).create();

        setName();
        setPermission();
        setBuilders();
        setIcon();
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
        } else if(fieldType == FieldType.PERMISSION) {
            this.permission = entry;
        }
        fieldType = null;
    }

    @Override
    public void update() {
        setName();
        setPermission();
        setBuilders();
        setIcon();
        gui.update();
    }

    /**
     *
     */
    private void setName() {
        gui.setItem(10, PaperItemBuilder.from(Material.NAME_TAG)
                .name(name)
                .asGuiItem(event -> {
                    if(warp == null) {
                        UUID uuid = player.getUniqueId();
                        cache.addEditingGui(uuid, this);
                        cache.addAwaitingChat(uuid);
                        gui.close(player);
                        fieldType = FieldType.NAME;
                        player.sendMessage(PlaceholderParser.of(config.WARP_WARP_EDITING_NAME).prefix(config.PREFIX).parse());
                    }
                    event.setCancelled(true);
                }));
    }

    /**
     *
     */
    private void setPermission() {
        gui.setItem(12, PaperItemBuilder.from(Material.TRIAL_KEY)
                .name(permission)
                .asGuiItem(event -> {
                    UUID uuid = player.getUniqueId();
                    cache.addEditingGui(uuid, this);
                    cache.addAwaitingChat(uuid);
                    gui.close(player);
                    fieldType = FieldType.PERMISSION;
                    player.sendMessage(PlaceholderParser.of(config.WARP_WARP_EDITING_PERMISSION).prefix(config.PREFIX).parse());
                    event.setCancelled(true);
                }));
    }

    /**
     *
     */
    private void setBuilders() {
        gui.setItem(14, PaperItemBuilder.from(Material.PLAYER_HEAD)
                .name(builder)
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    UUID uuid = player.getUniqueId();
                    cache.addEditingGui(uuid, this);
                    cache.addAwaitingChat(uuid);
                    gui.close(player);
                    fieldType = FieldType.BUILDERS;
                    player.sendMessage(PlaceholderParser.of(config.WARP_WARP_EDITING_BUILDERS).prefix(config.PREFIX).parse());
                }));
    }

    /**
     *
     */
    private void setIcon() {
        gui.setItem(16, guiItem);
        gui.addSlotAction(16, event -> {
            ItemStack clickedItem = event.getCursor();
            if(clickedItem.getType() != Material.AIR) {
                gui.updateItem(16, clickedItem);
            }
            event.setCancelled(true);
        });
    }

    /**
     *
     */
    private void confirmButton() {
        gui.setItem(30, PaperItemBuilder.from(Material.EMERALD)
                .name(TextUtils.stripDecoration("Confirm!").color(NamedTextColor.GREEN))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    WGui hopBackTo;
                    ItemStack itemStack = gui.getGuiItem(16).getItemStack();

                    // Verify the item was changed
                    if(itemStack.getType().equals(Material.GRAY_STAINED_GLASS_PANE)) return;

                    Warp tempWarp;

                    if(warp != null) {
                        warp.setItemStack(itemStack);
                        warp.setLocation(player.getLocation());
                        warp.setBuilder(mm.serialize(builder));
                        tempWarp = warp;
                        hopBackTo = lastGui;
                    } else {
                        Warp newWarp = new Warp(mm.serialize(name),
                                PlainTextComponentSerializer.plainText().serialize(permission),
                                player.getLocation(),
                                mm.serialize(builder),
                                itemStack);
                        cache.addWarp(newWarp);
                        tempWarp = newWarp;
                        hopBackTo = this;
                    }
                    warpTable.save(tempWarp);
                    suppressCloseEvent = true;
                    hopBackTo.getLast().nullify();
                    hopBackTo.openLast();
                    suppressCloseEvent = false;
                }));
    }

    /**
     *
     */
    private void cancelButton() {
        gui.setItem(32, PaperItemBuilder.from(Material.BARRIER)
                .name(TextUtils.stripDecoration("Cancel").color(NamedTextColor.RED))
                .asGuiItem(event -> {
                    WGui hopBackTo;
                    if(warp != null) hopBackTo = lastGui;
                    else hopBackTo = this;
                    suppressCloseEvent = true;
                    hopBackTo.openLast();
                    suppressCloseEvent = false;
                }));
    }
}
