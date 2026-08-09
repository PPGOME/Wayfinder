package me.ppgome.wayfinder.guis.home;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.database.Home;
import me.ppgome.wayfinder.database.PlayerMeta;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.List;

public class HomesGui extends WGui {

    PlayerMeta playerMeta;
    boolean forSelf;

    public HomesGui(PlayerMeta playerMeta, boolean forSelf) {
        this.playerMeta = playerMeta;
        this.forSelf = forSelf;
    }

    @Override
    protected Gui buildGui(Player player) {

        List<Home> homeList = GuiUtils.mapToList(playerMeta.getAllHomes());

        String title = forSelf ? "Your homes" : playerMeta.getName() + "'s homes";

        gui = Gui.gui()
                .title(Component.text(title)
                        .color(NamedTextColor.BLUE)
                        .decoration(TextDecoration.BOLD, true))
                .rows(5)
                .create();

        GuiUtils.insertGrayPanes(gui, 10, 34);
        GuiUtils.fillWhitePanes(gui);
        GuiUtils.defaultCancel(gui);

        int slot = 10;

        for(Home home : homeList) {
            while(GuiUtils.isBorder(slot)) {
                slot++;
            }
            GuiUtils.addHomeItem(this, gui, slot, home, player);
            slot++;
        }

        addHome();

        return gui;
    }

    private void addHome() {
        gui.setItem(
                40,
                ItemBuilder.from(GuiUtils.getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19",
                        Component.text("New home from current position", NamedTextColor.GREEN))).asGuiItem(inventoryClickEvent -> {
                            openNext(player, config, cache, new HomeBuilderGui());
                })
        );
    }

}