package io.github.densamisten.client;

import io.github.densamisten.gui.CaesarCipherScreen;
import net.minecraft.client.Minecraft;

public class ClientHooks {
    public static void openExampleBlockScreen() {
        Minecraft.getInstance().setScreen(new CaesarCipherScreen());
    }
}
