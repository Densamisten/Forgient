package io.github.densamisten.command;

import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CopyRawDataCommand {
    private static final ClipboardManager clipboardManager = new ClipboardManager();


    public CopyRawDataCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("copyraw")
                .then(Commands.argument("file", StringArgumentType.string())
                        .executes(CopyRawDataCommand::copyRawData)
                )
        );
    }

    private static int copyRawData(CommandContext<CommandSourceStack> context) {
        String filePath = context.getArgument("file", String.class);
        try {
            byte[] rawData = Files.readAllBytes(Path.of(filePath));
            clipboardManager.setClipboard(GLFW.glfwGetCurrentContext(), bytesToHexString(rawData));
            context.getSource().sendSuccess(() -> Component.literal("Raw data copied to clipboard."), false);
        } catch (IOException e) {
            context.getSource().sendFailure(Component.literal("Failed to read file: " + e.getMessage()));
        }
        return 1;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}