package io.github.densamisten.gui;

import io.github.densamisten.Forgient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CaesarCipherScreen extends Screen {
    private static final Component TITLE =
            Component.translatable("title." + Forgient.MOD_ID + ".caesar_cipher_screen");
    private static final Component EXAMPLE_BUTTON =
            Component.translatable("gui." + Forgient.MOD_ID + ".button");

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Forgient.MOD_ID, "textures/entity/example_entity.png");

    private final int imageWidth, imageHeight;

    private int leftPos, topPos;

    private Button button;
    private EditBox editBox; // Add an EditBox field
    private EditBox editBox2; // New EditBox field


    public CaesarCipherScreen() {
        super(TITLE);

        this.imageWidth = 176;
        this.imageHeight = 166;

    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        if(this.minecraft == null) return;
        Level level = this.minecraft.level;
        if(level == null) return;

        // Initialize the EditBox
        this.editBox = new EditBox(this.font, this.leftPos + 8, this.topPos + 40, 100, 20, Component.literal("Input text"));
        this.editBox.setMaxLength(128);
        this.editBox.setResponder((p_98305_) -> {
            // Action when the text changes
        });

        // Initialize the second EditBox
        this.editBox2 = new EditBox(this.font, this.leftPos + 8, this.topPos + 70, 100, 20, Component.literal("Input shift"));
        this.editBox2.setMaxLength(128);
        this.editBox2.setResponder((p_98305_) -> {
            // Action when the text changes
        });
        this.addWidget(this.editBox2);

        this.addWidget(this.editBox);

        // Add the button
        this.button = addRenderableWidget(
                Button.builder(
                                EXAMPLE_BUTTON,
                                this::handleExampleButton)
                        .bounds(this.leftPos + 40, this.topPos + 100, 80, 20)
                        .tooltip(Tooltip.create(EXAMPLE_BUTTON))
                        .build());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderTransparentBackground(graphics);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawString(this.font,
                TITLE,
                this.leftPos + 32,
                this.topPos + 8,
                0x404040,
                false);

        graphics.drawString(this.font,
                "Message",
                this.leftPos + 110,
                this.topPos + 48,
                0xAB09F5,
                false);
        graphics.drawString(this.font,
                "Shift",
                this.leftPos + 110,
                this.topPos + 80,
                0xAB09F5,
                false);

        // Render the EditBox
        this.editBox.render(graphics, mouseX, mouseY, partialTicks);
        this.editBox2.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void handleExampleButton(Button button) {
        String inputText = this.editBox.getValue();
        String shiftValueStr = this.editBox2.getValue();
        int shiftValue = 0;

        // Parse the shift value from EditBox2
        try {
            shiftValue = Integer.parseInt(shiftValueStr);
        } catch (NumberFormatException e) {
            // Handle invalid shift value
            return;
        }

        // Perform Caesar cipher encryption or decryption
        String resultText = encryptOrDecrypt(inputText, shiftValue);

        // Display the result in the chat
        Minecraft.getInstance().player.connection.sendChat(resultText);
    }

    private String encryptOrDecrypt(String text, int shift) {
        StringBuilder resultText = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch)) {
                char shifted = (char) (ch + shift);
                if (Character.isLowerCase(ch)) {
                    if (shifted > 'z') {
                        shifted = (char) ('a' + (shifted - 'z' - 1));
                    } else if (shifted < 'a') {
                        shifted = (char) ('z' - ('a' - shifted - 1));
                    }
                } else if (Character.isUpperCase(ch)) {
                    if (shifted > 'Z') {
                        shifted = (char) ('A' + (shifted - 'Z' - 1));
                    } else if (shifted < 'A') {
                        shifted = (char) ('Z' - ('A' - shifted - 1));
                    }
                }
                resultText.append(shifted);
            } else {
                resultText.append(ch);
            }
        }
        return resultText.toString();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}