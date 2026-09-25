package me.ppgome.wayfinder.guis.place;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.text.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class PlaceBuilderCategoryGui extends WGui {

    private Component placeName;
    Map<String, Category> allCategories;
    private Map<String, Category> selectedCategories;

    public PlaceBuilderCategoryGui(Component placeName, Map<String, Category> selectedCategories) {
        this.placeName = placeName;
        this.selectedCategories = selectedCategories;
    }

    @Override
    protected Gui buildGui(Player player) {

        this.allCategories = cache.getAllCategories();

        gui = Gui.gui().title(Component.text("Pick categories for ").append(placeName)).rows(4).create();

        GuiUtils.insertGrayPanes(gui, 10, 25);

        categoryButtons();
        confirmButton();

        GuiUtils.fillWhitePanes(gui);

        GuiUtils.defaultCancel(gui);

        return gui;
    }

    /**
     *
     */
    private void categoryButtons() {
        int slot = 10;
        for(Category category : allCategories.values()) {
            while(GuiUtils.isBorder(slot)) {
                slot++;
            }
            String catName = category.getName();
            gui.setItem(slot, PaperItemBuilder.from(category.getItemStack())
                    .name(Component.text(catName, categoryColour(catName))
                            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).asGuiItem());
            gui.addSlotAction(slot, event -> {
                event.setCancelled(true);
                if(selectedCategories.containsKey(catName)) {
                    selectedCategories.remove(catName);
                } else {
                    selectedCategories.put(catName, allCategories.get(catName));
                }

                ItemStack item = gui.getGuiItem(event.getSlot()).getItemStack();
                item.editMeta(itemMeta -> {
                    Component metaName = itemMeta.displayName();
                    itemMeta.displayName(metaName.color(categoryColour(PlainTextComponentSerializer.plainText().serialize(metaName))));
                    itemMeta.lore(List.of(Component.text("Priority: " + category.getPriority())));
                });

                gui.updateItem(event.getSlot(), item);
            });
            slot++;
        }
    }

    private NamedTextColor categoryColour(String name) {
        return selectedCategories.containsKey(name) ? NamedTextColor.GREEN : NamedTextColor.RED;
    }

    /**
     *
     */
    private void confirmButton() {
        gui.setItem(30, PaperItemBuilder.from(Material.EMERALD)
                .name(TextUtils.stripDecoration("Confirm!").color(NamedTextColor.GREEN))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    if(lastGui instanceof PlaceBuilderGui placeBuilderGui) placeBuilderGui.updateCategories(selectedCategories);
                    this.openLast();
                }));
    }
}
