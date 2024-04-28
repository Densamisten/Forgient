package io.github.densamisten.command.vanilla;

import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class VerboseSayCommand {
    private static final ClipboardManager clipboardManager = new ClipboardManager();

    public VerboseSayCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vsay")
                .then(Commands.argument("color", ColorArgument.color())
                        .executes(context -> sendVerboseMessage(context.getSource(), ColorArgument.getColor(context, "color")))));
    }

    // Our executing method
    private static int sendVerboseMessage(CommandSourceStack source, ChatFormatting color) {
        // Get the clipboard contents
        String clipboardContents = clipboardManager.getClipboard(GLFW.glfwGetCurrentContext(), GLFWErrorCallback.createPrint(System.err));
        // Perform send message action along given style allowed with ChatFormatting
        source.sendSuccess(() -> Component.literal(clipboardContents).withStyle(color), false);
        // Return 1 for success
        return 1;
    }
}
