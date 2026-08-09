package me.ppgome.wayfinder.guis;

import dev.triumphteam.gui.guis.Gui;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public abstract class WGui {

    protected Gui gui;
    protected WGui lastGui;
    protected WConfig config;
    protected Cache cache;
    protected Player player;
    protected MiniMessage mm = MiniMessage.miniMessage();

    protected abstract Gui buildGui(Player player);

    public void display(Player player) {
        this.player = player;
        this.cache = Wayfinder.PLUGIN.getCache();
        if(gui == null) {
            gui = buildGui(player);
        }
        gui.open(player);
    }

    public void openNext(Player player, WConfig config, Cache cache, WGui next) {
        next.lastGui = this;
        next.config = config;
        next.cache = cache;
        next.player = player;
        next.display(player);
    }

    public void openLast() {
        if (lastGui != null) {
            lastGui.display(player);
        }
    }

    public WGui getLast() {
        return lastGui;
    }

    public void nullify() {
        gui = null;
    }

}
