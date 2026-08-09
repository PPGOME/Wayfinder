package me.ppgome.wayfinder.guis;

import net.kyori.adventure.text.Component;

public interface AcceptsChatGui {

    void submitChatEntry(Component entry);

    void update();

}
