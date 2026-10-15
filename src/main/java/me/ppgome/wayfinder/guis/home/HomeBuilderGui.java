package me.ppgome.wayfinder.guis.home;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.Home;
import me.ppgome.wayfinder.database.HomeTable;
import me.ppgome.wayfinder.guis.AcceptsChatGui;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.text.PlaceholderParser;
import me.ppgome.wayfinder.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class HomeBuilderGui extends WGui implements AcceptsChatGui {

    private Component name;
    private GuiItem guiItem = PaperItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(TextUtils.stripDecoration(Component.text("Icon (click with item)")))
            .asGuiItem();

    private Home home;

    private final HomeTable homeTable = Wayfinder.PLUGIN.getDatabase().getHomeTable();

    private boolean suppressCloseEvent = false;

    public HomeBuilderGui() {
        this.name = Component.text("Click here to name...");
    }

    public HomeBuilderGui(Home home) {
        this.home = home;
        this.name = Component.text(home.getName());
        this.guiItem = PaperItemBuilder.from(home.getItemStack()).asGuiItem();
    }

    @Override
    protected Gui buildGui(Player player) {

        boolean editing = home != null;

        Component title;
        if(editing) title = PlaceholderParser.of(config.GUI_HOME_HOME_BUILDER).parse();
        else title = Component.text("Creating new place...");

        gui = Gui.gui().title(title).rows(3).create();

        setName();
        setIcon();
        confirmButton();
        cancelButton();

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

    @Override
    public void submitChatEntry(Component entry) {
        this.name = entry;
    }

    @Override
    public void update() {
        setName();
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
                    event.setCancelled(true);
                    if(home == null) {
                        UUID uuid = player.getUniqueId();
                        cache.addEditingGui(uuid, this);
                        cache.addAwaitingChat(uuid);
                        gui.close(player);
                        player.sendMessage(PlaceholderParser.of(config.PLACE_PLACE_EDITING_NAME).prefix(config.PREFIX).parse());
                    }
                }));
    }

    /**
     *
     */
    private void setIcon() {
        gui.setItem(12, guiItem);
        gui.addSlotAction(12, event -> {
            ItemStack clickedItem = event.getCursor();
            if(clickedItem.getType() != Material.AIR) {
                gui.updateItem(12, clickedItem);
            }
            event.setCancelled(true);
        });
    }

    /**
     *
     */
    private void confirmButton() {
        gui.setItem(14, PaperItemBuilder.from(Material.EMERALD)
                .name(TextUtils.stripDecoration("Confirm!").color(NamedTextColor.GREEN))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    WGui hopBackTo;
                    ItemStack itemStack = gui.getGuiItem(12).getItemStack();

                    // Verify the item was changed
                    if(itemStack.getType().equals(Material.GRAY_STAINED_GLASS_PANE)) return;

                    Home tempHome;

                    if(home != null) {
                        home.setItemStack(itemStack);
                        tempHome = home;
                        hopBackTo = lastGui;
                    } else {
                        Home newHome = new Home(player.getUniqueId(), mm.serialize(name), itemStack, player.getLocation());
                        cache.getPlayerMeta(player.getUniqueId()).addHome(newHome);
                        tempHome = newHome;
                        hopBackTo = this;
                    }
                    homeTable.save(tempHome);
                    suppressCloseEvent = true;
                    hopBackTo.getLast().nullify();
                    hopBackTo.openLast();
                    cache.removeEditingGui(player.getUniqueId());
                    suppressCloseEvent = false;
                }));
    }

    /**
     *
     */
    private void cancelButton() {
        gui.setItem(16, PaperItemBuilder.from(Material.BARRIER)
                .name(TextUtils.stripDecoration("Cancel").color(NamedTextColor.RED))
                .asGuiItem(event -> {
                    WGui hopBackTo;
                    if(home != null) hopBackTo = lastGui;
                    else hopBackTo = this;
                    suppressCloseEvent = true;
                    hopBackTo.openLast();
                    suppressCloseEvent = false;
                }));
    }

}
