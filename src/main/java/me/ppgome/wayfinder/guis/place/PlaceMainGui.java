package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.guis.EditOrDeleteGui;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.text.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
                        PaperItemBuilder.from(tempCat.getItemStack())
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

        settingsButton();

        // Stop clicks from doing anything normally
        GuiUtils.defaultCancel(gui);

        if(player.hasPermission("wayfinder.admin.place")) {
            gui.setItem(isFiveRows ? 36 : 45,
                    PaperItemBuilder.from(Material.COMPASS)
                            .name(Component.text("New Place"))
                            .asGuiItem(event -> {
                                openNext(player, config, cache, new PlaceBuilderGui());
                            }));
            gui.setItem(isFiveRows ? 44 : 53,
                    PaperItemBuilder.from(Material.CHEST)
                            .name(Component.text("New Category"))
                            .asGuiItem(event -> {
                                openNext(player, config, cache, new CategoryBuilderGui());
                            }));
        }

        return gui;
    }

    private void settingsButton() {
        ItemStack buttonItem = GuiUtils.getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRkNDliYWU5NWM3OTBjM2IxZmY1YjJmMDEwNTJhNzE0ZDYxODU0ODFkNWIxYzg1OTMwYjNmOTlkMjMyMTY3NCJ9fX0=",
                Component.text("Settings").decoration(TextDecoration.ITALIC, false));
        GuiItem guiItem = PaperItemBuilder
                .from(buttonItem)
                .asGuiItem(event -> openNext(player, config, cache, new PlaceSettingsGui(player)));
        gui.setItem(40, guiItem);
    }
}
