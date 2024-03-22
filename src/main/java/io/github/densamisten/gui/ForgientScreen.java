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

public class ForgientScreen extends Screen {
    private static final Component TITLE =
            Component.translatable("gui." + Forgient.MOD_ID + ".forgient_screen");
    private static final Component EXAMPLE_BUTTON =
            Component.translatable("gui." + Forgient.MOD_ID + ".button");

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Forgient.MOD_ID, "textures/gui/example_block.png");

    private final int imageWidth, imageHeight;

    private int leftPos, topPos;

    private Button button;
    private EditBox editBox; // Add an EditBox field

    public ForgientScreen() {
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
        this.editBox = new EditBox(this.font, this.leftPos + 8, this.topPos + 40, 100, 20, Component.nullToEmpty("123"));
        this.editBox.setMaxLength(128);
        this.editBox.setResponder((p_98305_) -> {
            // Action when the text changes
        });
        this.addWidget(this.editBox);

        // Add the button
        this.button = addRenderableWidget(
                Button.builder(
                                EXAMPLE_BUTTON,
                                this::handleExampleButton)
                        .bounds(this.leftPos + 8, this.topPos + 20, 80, 20)
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
                this.leftPos + 8,
                this.topPos + 8,
                0x404040,
                false);

        graphics.drawString(this.font,
                "Locale" + Minecraft.getInstance().getLocale(),
                this.leftPos + 16,
                this.topPos + 88,
                0xFF0000,
                false);

        // Render the EditBox
        this.editBox.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void handleExampleButton(Button button) {
        String text = this.editBox.getValue(); // Retrieve the text from the EditBox
        if (!text.isEmpty()) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal(text), false); // Send the text to the chat
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
