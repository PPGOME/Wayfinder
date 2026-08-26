package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.guis.EditOrDeleteGui;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.text.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class PlaceMainGui extends WGui {

    //------------------------------------------------------------------------------------------------------------------

    public PlaceMainGui(WConfig config, Cache cache) {
        this.config = config;
        this.cache = cache;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    protected Gui buildGui(Player player) {

        Map<String, Category> categories = cache.getAllCategories();
        int numberOfCategories;
        if(categories != null) {
            numberOfCategories = categories.size();
        } else {
            numberOfCategories = 0;
        }

        Map<String, Place> pinnedPlaces = cache.getAllPinnedPlaces();

        int rows = (numberOfCategories > 9) ? 6 : 5;
        boolean isFiveRows = (rows == 5);

        gui = Gui.gui().title(Component.text("Place Categories")).rows(rows).create();

        // Fill in remaining space
        GuiUtils.fillWhitePanes(gui);

        // Place gray panes for empty slots
        GuiUtils.insertGrayPanes(gui, 10, isFiveRows ? 16 : 25);
        GuiUtils.insertGrayPanes(gui, isFiveRows ? 28 : 37, isFiveRows ? 34 : 43);

        if(numberOfCategories > 0) {
            // Place category items
            int[] categorySlots = GuiUtils.getSlotPlacement(numberOfCategories, 7, isFiveRows ? 1 : 2, 10);

            List<Category> categoryList = GuiUtils.mapToList(categories);
            for(int i = 0; i < categoryList.size(); i++) {
                Category tempCat = categoryList.get(i);
                gui.setItem(categorySlots[i],
                        ItemBuilder.from(tempCat.getItemStack())
                                .name(TextUtils.stripDecoration(tempCat.getName()))
                                .asGuiItem(event -> {
                                    if(event.isShiftClick()) {
                                        openNext(player, config, cache, new EditOrDeleteGui<>(tempCat));
                                    } else {
                                        openNext(player, config, cache, new PlaceListGui(tempCat));
                                    }
                                }));
            }
        }

        // Place pinned items
        int slot = (rows == 5) ? 28 : 37;
        for(Place place : pinnedPlaces.values()) {
            GuiUtils.addPlaceItem(this, gui, slot, place, player);
            slot++;
        }

        // Stop clicks from doing anything normally
        GuiUtils.defaultCancel(gui);

        if(player.hasPermission("wayfinder.admin.place")) {
            gui.setItem(isFiveRows ? 36 : 45,
                    ItemBuilder.from(Material.COMPASS)
                            .name(Component.text("New Place"))
                            .asGuiItem(event -> {
                                openNext(player, config, cache, new PlaceBuilderGui());
                            }));
            gui.setItem(isFiveRows ? 44 : 53,
                    ItemBuilder.from(Material.CHEST)
                            .name(Component.text("New Category"))
                            .asGuiItem(event -> {
                                openNext(player, config, cache, new CategoryBuilderGui());
                            }));
        }

        return gui;
    }
}
