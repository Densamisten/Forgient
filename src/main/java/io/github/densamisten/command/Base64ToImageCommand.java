package io.github.densamisten.command;

import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class Base64ToImageCommand {

    // Set an instance for the clipboard
    private final ClipboardManager clipboardManager = new ClipboardManager();

    public Base64ToImageCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("b642img")
                .executes(context -> convertClipboardToImage(context.getSource()))
        );
    }

    private int convertClipboardToImage(CommandSourceStack source) {
        // Get the clipboard
        String clipboardContents = clipboardManager.getClipboard(GLFW.glfwGetCurrentContext(), GLFWErrorCallback.createPrint(System.err));

        // Decode the base64 contents and set to a variable
        byte[] imageBytes = Base64.getDecoder().decode(clipboardContents);

        try {
            String imageType = getImageType(imageBytes);
            if (imageType.equals("PNG") || imageType.equals("JPEG")) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                if (img != null) {
                    File outputFile = new File("downloads/decoded_image." + imageType.toLowerCase());
                    ImageIO.write(img, imageType, outputFile);
                    source.sendSuccess(() -> Component.literal("Image successfully decoded and saved as 'decoded_image." + imageType.toLowerCase() + "'"), true);
                    return 1;
                }
            } else {
                source.sendFailure(Component.literal("Unknown image type."));
            }
        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to decode image from clipboard: " + e.getMessage()));
        }

        return 0;
    }

    private String getImageType(byte[] imageData) {
        if (imageData.length >= 8) {
            if (imageData[0] == (byte) 0x89 && imageData[1] == (byte) 0x50 && imageData[2] == (byte) 0x4E && imageData[3] == (byte) 0x47 && imageData[4] == (byte) 0x0D && imageData[5] == (byte) 0x0A && imageData[6] == (byte) 0x1A && imageData[7] == (byte) 0x0A) {
                return "PNG";
            } else if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8 && imageData[2] == (byte) 0xFF && imageData[3] == (byte) 0xE0 && imageData[6] == (byte) 'J' && imageData[7] == (byte) 'F' && imageData[8] == (byte) 'I' && imageData[9] == (byte) 'F') {
                return "JPEG";
            }
        }
        return "Unknown";
    }
}
