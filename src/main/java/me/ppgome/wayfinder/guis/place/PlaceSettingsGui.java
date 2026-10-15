package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.guis.Gui;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.PlayerMeta;
import me.ppgome.wayfinder.database.PlayerMetaTable;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.List;

public class PlaceSettingsGui extends WGui {

    private PlayerMeta playerMeta;
    private boolean particles;
    private boolean arrow;
    private boolean portals;

    private static final List<String> PARTICLE_LORE =
            List.of("Click to toggle the", "particle trail when", "navigating to a place");
    private static final List<String> ARROW_LORE =
            List.of("Click to toggle the", "floating arrow when", "navigating to a place");
    private static final List<String> PORTAL_LORE =
            List.of("Click to toggle the End", "& Nether portal shortcuts", "when navigating to a place");

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

        GuiUtils.fillWhitePanes(gui);
        particleButton();
        arrowButton();
        portalButton();
        confirmButton();

        return gui;
    }

    private void particleButton() {
        gui.setItem(10, GuiUtils.toggleButton("Particle guide", particles,
                potion(Material.SPLASH_POTION, particles), PARTICLE_LORE, event -> {
            particles = !particles;
            particleButton();
            gui.update();
        }));
    }

    private void arrowButton() {
        gui.setItem(12, GuiUtils.toggleButton("Arrow guide", arrow,
                potion(Material.TIPPED_ARROW, arrow), ARROW_LORE, event -> {
            arrow = !arrow;
            arrowButton();
            gui.update();
        }));
    }

    private void portalButton() {
        gui.setItem(14, GuiUtils.toggleButton("Portal shortcuts", portals,
                new ItemStack(portals ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN),
                PORTAL_LORE, event -> {
            portals = !portals;
            portalButton();
            gui.update();
        }));
    }

    private void confirmButton() {
        gui.setItem(16, GuiUtils.button(Material.EMERALD, "Save settings", NamedTextColor.GREEN,
                event -> {
            event.setCancelled(true);
            playerMeta.setPlaceParticlesEnabled(particles);
            playerMeta.setPlaceArrowEnabled(arrow);
            playerMeta.setEnablePortalSkipping(portals);
            playerMetaTable.save(playerMeta);
            this.openLast();
        }));
    }

    // -----------------------------------------------------------------------------------------------------------------

    private ItemStack potion(Material material, boolean on) {
        ItemStack item = new ItemStack(material);
        item.editMeta(PotionMeta.class, meta ->
                meta.setColor(on ? Color.fromRGB(7, 255, 7) : Color.fromRGB(255, 0, 0)));
        return item;
    }


}
