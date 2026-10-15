package me.ppgome.wayfinder.guis;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import me.ppgome.wayfinder.text.PlaceholderParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.UUID;

public abstract class EditorGui extends WGui implements AcceptsChatGui {

    protected FieldType type;

    private boolean suppressCloseEvent = false;

    protected void installEditorCloseAction(){
        gui.setCloseGuiAction(event -> {
            if(!suppressCloseEvent) {
                cache.addEditingGui(player.getUniqueId(), this);
            }
        });
    }

    protected GuiItem chatButton(PaperItemBuilder builder, FieldType fieldType, String message) {
        return builder.asGuiItem(event -> {
            event.setCancelled(true);
            promptChat(fieldType, message);
        });
    }
    
    protected void promptChat(FieldType fieldType, String message) {
        UUID uuid = player.getUniqueId();
        cache.addEditingGui(uuid, this);
        cache.addAwaitingChat(uuid);
        fieldType = type;
        gui.close(player);
        player.sendMessage(PlaceholderParser.of(message).prefix(config.PREFIX).parse());
    }

    protected void leave(boolean editing, boolean refreshParent) {
        WGui hopBackTo = editing ? lastGui : this;
        suppressCloseEvent = true;
        if (refreshParent) hopBackTo.getLast().nullify();
        hopBackTo.openLast();
        suppressCloseEvent = false;
        cache.removeEditingGui(player.getUniqueId());
    }

    protected void reject(String message) {
        player.sendMessage(Component.text(message, NamedTextColor.RED));
    }

    protected static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }


}
