package me.ppgome.wayfinder.guis.warp;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.database.Warp;
import me.ppgome.wayfinder.guis.GuiUtils;
import me.ppgome.wayfinder.guis.WGui;
import me.ppgome.wayfinder.guis.place.PlaceBuilderGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class WarpMainGui extends WGui {

    //------------------------------------------------------------------------------------------------------------------

    public WarpMainGui(WConfig config, Cache cache) {
        this.config = config;
        this.cache = cache;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    protected Gui buildGui(Player player) {

        Map<String, Warp> warpMap = cache.getAllWarps();

        List<Warp> warps = GuiUtils.mapToList(warpMap);

        gui = Gui.gui()
                .title(Component.text("Warps")
                        .color(NamedTextColor.BLUE)
                        .decoration(TextDecoration.BOLD, true))
                .rows(6)
                .create();

        GuiUtils.insertGrayPanes(gui, 10, 43);
        GuiUtils.fillWhitePanes(gui);
        GuiUtils.defaultCancel(gui);

        int slot = 10;

        for(Warp warp : warps) {
            if(player.hasPermission(warp.getPermission())) {
                while(GuiUtils.isBorder(slot)) {
                    slot++;
                }
                GuiUtils.addWarpItem(this, gui, slot, warp, player);
                slot++;
            }
        }

        if(player.hasPermission("wayfinder.admin.warp")) {
            gui.setItem(49,
                    PaperItemBuilder.from(Material.COMPASS)
                            .name(Component.text("New Warp"))
                            .asGuiItem(event -> {
                                openNext(player, config, cache, new WarpBuilderGui());
                            }));
        }

        return gui;
    }
}
