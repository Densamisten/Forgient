package io.github.densamisten.command;

import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class Hex2ImgCommand {
    private static final ClipboardManager clipboardManager = new ClipboardManager();

    public Hex2ImgCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hex2img")
                .executes(context -> {
                    try {
                        return execute(context.getSource());
                    } catch (DecoderException | IOException | AWTException e) {
                        throw new RuntimeException(e);
                    }
                })
        );
    }

    private static int execute(CommandSourceStack source) throws DecoderException, IOException, AWTException {
        String clipboard = clipboardManager.getClipboard(GLFW.glfwGetCurrentContext(), GLFWErrorCallback.createPrint(System.err));
        if (clipboard.isEmpty()) {
            source.sendFailure(Component.literal("Clipboard is empty."));
            return 0;
        }

        try {
            byte[] imageData = hexStringToBytes(clipboard);
            BufferedImage image = loadImageFromBytes(imageData);
            saveImageToFile(image, "output.png");
            return 1;
        } catch (DecoderException | IOException e) {
            throw new DecoderException("Failed to convert hex to image: " + e.getMessage());
        }
    }

    private static byte[] hexStringToBytes(String hexString) throws DecoderException {
        return Hex.decodeHex(hexString.toCharArray());
    }

    private static BufferedImage loadImageFromBytes(byte[] imageData) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(imageData));
    }

    private static void saveImageToFile(BufferedImage image, String filename) throws IOException {
        ImageIO.write(image, "png", new File(filename));
    }
}
