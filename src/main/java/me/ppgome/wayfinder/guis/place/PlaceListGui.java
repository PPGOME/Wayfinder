package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.text.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlaceListGui extends WGui {

    private Category category;

    //------------------------------------------------------------------------------------------------------------------

    public PlaceListGui(Category category) {
        this.category = category;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    protected Gui buildGui(Player player) {

        Set<Place> placeSet = category.getPlaces();
        Map<String, Place> placeMap = new HashMap<>();

        for(Place place : placeSet) {
            placeMap.put(place.getName(), place);
        }

        List<Place> places = GuiUtils.mapToList(placeMap);

        gui = Gui.gui()
                .title(Component.text("Places in ")
                        .append(Component.text(category.getName())
                                .color(NamedTextColor.BLUE)
                                .decoration(TextDecoration.BOLD, true)))
                .rows(6)
                .create();

        GuiUtils.insertGrayPanes(gui, 10, 43);
        GuiUtils.fillWhitePanes(gui);
        GuiUtils.defaultCancel(gui);

        int slot = 10;

        for(Place place : places) {
            while(GuiUtils.isBorder(slot)) {
                slot++;
            }
            GuiUtils.addPlaceItem(this, gui, slot, place, player);
            slot++;
        }

        cancelButton();

        return gui;
    }

    //------------------------------------------------------------------------------------------------------------------

    private void cancelButton() {
        gui.setItem(49, ItemBuilder.from(Material.BARRIER)
                .name(TextUtils.stripDecoration("Cancel").color(NamedTextColor.RED))
                .asGuiItem(event -> this.openLast()));
    }
}
