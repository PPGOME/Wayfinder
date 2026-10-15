package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.PlayerMeta;
import me.ppgome.wayfinder.database.PlayerMetaTable;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.text.PlaceholderParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.List;

public class PlaceSettingsGui extends WGui {

    private PlayerMeta playerMeta;
    private boolean particles;
    private boolean arrow;
    private boolean portals;

    private PlayerMetaTable playerMetaTable;

    public PlaceSettingsGui(Player player) {
    }

    @Override
    protected Gui buildGui(Player player) {
        this.playerMeta = cache.getPlayerMeta(player.getUniqueId());
        this.particles = playerMeta.isPlaceParticlesEnabled();
        this.arrow = playerMeta.isPlaceArrowEnabled();
        this.portals = playerMeta.isEnablePortalSkipping();
        this.playerMetaTable = Wayfinder.PLUGIN.getDatabase().getPlayerMetaTable();

        Component title = Component.text("Your Place Settings", NamedTextColor.BLUE, TextDecoration.BOLD);

        gui = Gui.gui().title(title).rows(3).create();

        GuiUtils.fillWhitePanesEditor(gui);
        enableParticles();
        enableArrow();
        enablePortals();
        confirmButton();

        return gui;
    }

    private void update() {
        enableParticles();
        enableArrow();
        enablePortals();
        gui.update();
    }

    private void enableParticles() {

        String name;
        TextColor nameColour;
        Color colour;

        if(particles) {
            name = "Disable particle guide";
            nameColour = NamedTextColor.RED;
            colour = Color.fromRGB(7, 255, 7);
        } else {
            name = "Enable particle guide";
            nameColour = NamedTextColor.GREEN;
            colour = Color.fromRGB(255, 0, 0);
        }

        ItemStack buttonItem = new ItemStack(Material.SPLASH_POTION);
        PotionMeta buttonMeta = (PotionMeta) buttonItem.getItemMeta();
        if(buttonMeta != null) {
            buttonMeta.setColor(colour);
            buttonMeta.displayName(Component.text(name, nameColour));
            buttonItem.setItemMeta(buttonMeta);
        }

        GuiItem buttonGuiItem = PaperItemBuilder
                .from(buttonItem)
                .lore(List.of(
                        Component.text("Click to toggle the"),
                        Component.text("particle train when"),
                        Component.text("Navigating to a place")
                ))
                .asGuiItem(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    particles = !particles;
                    update();
                });
        gui.setItem(10, buttonGuiItem);

    }

    private void enableArrow() {

        String name;
        TextColor nameColour;
        Color colour;

        if(arrow) {
            name = "Disable arrow guide";
            nameColour = NamedTextColor.RED;
            colour = Color.fromRGB(7, 255, 7);
        } else {
            name = "Enable arrow guide";
            nameColour = NamedTextColor.GREEN;
            colour = Color.fromRGB(255, 0, 0);
        }

        ItemStack buttonItem = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta buttonMeta = (PotionMeta) buttonItem.getItemMeta();
        if(buttonMeta != null) {
            buttonMeta.setColor(colour);
            buttonMeta.displayName(Component.text(name, nameColour));
            buttonItem.setItemMeta(buttonMeta);
        }

        GuiItem buttonGuiItem = PaperItemBuilder
                .from(buttonItem)
                .lore(List.of(
                        Component.text("Click to toggle the"),
                        Component.text("floating arrow when"),
                        Component.text("Navigating to a place")
                ))
                .asGuiItem(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    arrow = !arrow;
                    update();
                });
        gui.setItem(12, buttonGuiItem);
    }

    private void enablePortals() {
        String name;
        TextColor nameColour;

        if(portals) {
            name = "Disable portal shortcuts";
            nameColour = NamedTextColor.RED;
        } else {
            name = "Enable portal shortcuts";
            nameColour = NamedTextColor.GREEN;
        }

        ItemStack buttonItem = new ItemStack(portals ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN);
        ItemMeta buttonMeta = buttonItem.getItemMeta();
        if(buttonMeta != null) {
            buttonMeta.displayName(Component.text(name, nameColour));
            buttonItem.setItemMeta(buttonMeta);
        }

        GuiItem buttonGuiItem = PaperItemBuilder
                .from(buttonItem)
                .lore(List.of(
                        Component.text("Click to toggle the End"),
                        Component.text("& Nether portal shortcuts"),
                        Component.text("Navigating to a place")
                ))
                .asGuiItem(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    portals = !portals;
                    update();
                });
        gui.setItem(14, buttonGuiItem);
    }

    private void confirmButton() {
        ItemStack buttonItem = new ItemStack(Material.EMERALD);
        ItemMeta buttonMeta = buttonItem.getItemMeta();
        if(buttonMeta != null) {
            buttonMeta.displayName(Component.text("Save settings", NamedTextColor.GREEN));
            buttonItem.setItemMeta(buttonMeta);
        }
        GuiItem buttonGuiItem = PaperItemBuilder
                .from(buttonItem)
                .asGuiItem(inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    playerMeta.setPlaceParticlesEnabled(particles);
                    playerMeta.setPlaceArrowEnabled(arrow);
                    playerMeta.setEnablePortalSkipping(portals);
                    playerMetaTable.save(playerMeta);
                    this.openLast();
                });
        gui.setItem(16, buttonGuiItem);
    }


}
