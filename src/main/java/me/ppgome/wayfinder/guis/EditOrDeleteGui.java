package me.ppgome.wayfinder.guis;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.Gui;
import me.ppgome.wayfinder.database.Home;
import me.ppgome.wayfinder.database.Warp;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.guis.home.HomeBuilderGui;
import me.ppgome.wayfinder.guis.place.CategoryBuilderGui;
import me.ppgome.wayfinder.guis.place.PlaceBuilderGui;
import me.ppgome.wayfinder.guis.warp.WarpBuilderGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class EditOrDeleteGui<T> extends WGui {
    
    private final T beingEdited;
    
    public EditOrDeleteGui(T beingEdited) {
        this.beingEdited = beingEdited;
    }

    @Override
    protected Gui buildGui(Player player) {

        gui = Gui.gui()
                .title(Component.text("Edit or delete?"))
                .type(GuiType.HOPPER)
                .create();

        gui.setItem(0, PaperItemBuilder.from(Material.EMERALD)
                .name(Component.text("Edit", NamedTextColor.GREEN))
                .asGuiItem(event -> {
                    if(beingEdited instanceof Category category) {
                        openNext(player, config, cache, new CategoryBuilderGui(category));
                    } else if(beingEdited instanceof Place place) {
                        openNext(player, config, cache, new PlaceBuilderGui(place));
                    } else if(beingEdited instanceof Home home) {
                        openNext(player, config, cache, new HomeBuilderGui(home));
                    } else if(beingEdited instanceof Warp warp) {
                        openNext(player, config, cache, new WarpBuilderGui(warp));
                    }
                }));

        gui.setItem(2, PaperItemBuilder.from(Material.FLINT_AND_STEEL)
                .name(Component.text("Delete", NamedTextColor.RED))
                .asGuiItem(event -> {
                    if(beingEdited instanceof Category category) {
                        if(!category.getPlaces().isEmpty()) {
                            player.sendMessage(
                                    Component.text("This category is not empty. Please remove all places from it.",
                                            NamedTextColor.RED)
                            );
                        }
                        cache.removeCategory(category);
                    }
                    else if(beingEdited instanceof Place place) cache.removePlace(place.getName());
                    else if(beingEdited instanceof Home home) cache.getPlayerMeta(player.getUniqueId())
                            .removeHome(home.getName());
                    else if(beingEdited instanceof Warp warp) cache.removeWarp(warp.getName());

                    lastGui.nullify();
                    openLast();
                }));

        gui.setItem(4, PaperItemBuilder.from(Material.BARRIER)
                .name(Component.text("Cancel", NamedTextColor.YELLOW))
                .asGuiItem(event -> openLast()));

        GuiUtils.fillWhitePanes(gui);
        GuiUtils.defaultCancel(gui);

        return gui;
    }
}
