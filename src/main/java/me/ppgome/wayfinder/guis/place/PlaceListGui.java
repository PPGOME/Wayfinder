package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.guis.Gui;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.navigation.ParticleNavigator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

import static me.ppgome.wayfinder.utils.PlaceUtils.findClosestPlace;

public class PlaceListGui extends WGui {

    private final Category category;

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

        gui = Gui.gui()
                .title(Component.text("Places in ")
                        .append(Component.text(category.getName())
                                .color(NamedTextColor.BLUE)
                                .decoration(TextDecoration.BOLD, true)))
                .rows(6)
                .create();

        GuiUtils.insertGrayPanes(gui);
        GuiUtils.fillWhitePanes(gui);
        GuiUtils.defaultCancel(gui);

        List<Place> places = category.getPlaces().stream()
                .sorted(Comparator.comparing(Place::getName)).toList();
        GuiUtils.placeAll(places, GuiUtils.interiorSlots(gui),
                (slot, place) -> GuiUtils.addPlaceItem(this, gui, slot, place, player));

        findNearestButton();
        gui.setItem(49, GuiUtils.cancel(event -> openLast()));

        return gui;
    }

    //------------------------------------------------------------------------------------------------------------------

    private void findNearestButton() {
        gui.setItem(4, GuiUtils.button(Material.COMPASS, "Find nearest " + category.getName() + " place", NamedTextColor.BLUE,
                event -> {
                    Place closestPlace = findClosestPlace(player.getLocation(), category);

                    if (closestPlace == null) {
                        player.sendMessage(Component.text("Unable to locate a place nearby"));
                        return;
                    }

                    new ParticleNavigator(Wayfinder.PLUGIN, player, closestPlace).startDisplaying();
                    this.gui.close(player);
                }
        ));
    }
}
