package me.ppgome.wayfinder.guis.place.editors;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlaceBuilderCategoryGui extends WGui {

    private final Component placeName;
    Map<String, Category> allCategories;
    private final Map<String, Category> selectedCategories;

    public PlaceBuilderCategoryGui(Component placeName, Map<String, Category> selectedCategories) {
        this.placeName = placeName;
        this.selectedCategories = new HashMap<>(selectedCategories);
    }

    @Override
    protected Gui buildGui(Player player) {

        this.allCategories = cache.getAllCategories();

        gui = Gui.gui().title(Component.text("Pick categories for ").append(placeName)).rows(4).create();

        GuiUtils.insertGrayPanes(gui);

        categoryButtons();
        confirmButton();

        GuiUtils.fillWhitePanes(gui);

        GuiUtils.defaultCancel(gui);

        return gui;
    }

    /**
     * Places the buttons for each category to the GUI
     */
    private void categoryButtons() {
        GuiUtils.placeAll(allCategories.values(), GuiUtils.interiorSlots(gui),
                (slot, category) -> gui.setItem(slot, categoryItem(category)));
    }

    /**
     * The item that represents a category
     * @param category The category being represented
     * @return An item that returns a category
     */
    private GuiItem categoryItem(Category category) {
        boolean selected = selectedCategories.containsKey(category.getName());
        return PaperItemBuilder.from(category.getItemStack())
                .name(TextUtils.stripDecoration(category.getName())
                        .color(selected ? NamedTextColor.GREEN : NamedTextColor.RED))
                .lore(Component.text("Priority: " + category.getPriority()))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    if (selected) selectedCategories.remove(category.getName());
                    else selectedCategories.put(category.getName(), category);
                    gui.updateItem(event.getSlot(), categoryItem(category));
                });
    }
    private void confirmButton() {
        gui.setItem(31, GuiUtils.confirm(event -> {
            cache.removeEditingGui(player.getUniqueId());
            if (lastGui instanceof PlaceBuilderGui placeBuilderGui) placeBuilderGui.updateCategories(selectedCategories);
            openLast();
        }));
    }
}
