package io.github.densamisten.command.vanilla;

import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class VerboseSayCommand {
    private static final ClipboardManager clipboardManager = new ClipboardManager();

    public VerboseSayCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vsay")
                .executes(ctx -> sendVerboseMessage(ctx.getSource())));
    }

    private static int sendVerboseMessage(CommandSourceStack source) {
        // Get the clipboard contents
        String clipboardContents = clipboardManager.getClipboard(GLFW.glfwGetCurrentContext(), GLFWErrorCallback.createPrint(System.err));
        // Perform send message action
        source.sendSuccess(() -> Component.literal(clipboardContents), false);
        // Return 1 for success
        return 1;
    }
}
