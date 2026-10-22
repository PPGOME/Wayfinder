package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
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
    private PaginatedGui paginatedGui;

    //------------------------------------------------------------------------------------------------------------------

    public PlaceListGui(Category category) {
        this.category = category;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    protected BaseGui buildGui(Player player) {

        Set<Place> placeSet = category.getPlaces();
        Map<String, Place> placeMap = new HashMap<>();

        for(Place place : placeSet) {
            placeMap.put(place.getName(), place);
        }

        paginatedGui = Gui.paginated()
                .title(Component.text("Places in ")
                        .append(Component.text(category.getName())
                                .color(NamedTextColor.BLUE)
                                .decoration(TextDecoration.BOLD, true)))
                .rows(6)
                .pageSize(28)
                .create();

        gui = paginatedGui;

        GuiUtils.defaultCancel(paginatedGui);
        paginatedGui.getFiller().fillBorder(GuiUtils.pane(Material.WHITE_STAINED_GLASS_PANE));

        category.getPlaces().stream()
                        .sorted(Comparator.comparing(Place::getName))
                                .forEach(place -> paginatedGui.addItem(GuiUtils.placeItem(this, place, player)));

        findNearestButton();
        previousButton();
        gui.setItem(49, GuiUtils.cancel(event -> openLast()));
        nextButton();

        return gui;
    }

    //------------------------------------------------------------------------------------------------------------------

    private void previousButton() {
        gui.setItem(47, PaperItemBuilder.from(Material.ARROW)
                .name(Component.text("Previous"))
                .asGuiItem(event -> {
                    paginatedGui.previous();
                })
        );
    }

    private void nextButton() {
        gui.setItem(51, PaperItemBuilder.from(Material.ARROW)
                .name(Component.text("Next"))
                .asGuiItem(event -> {
                    paginatedGui.next();
                })
        );
    }

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
