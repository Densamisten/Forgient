package io.github.densamisten.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.network.chat.Component;

public class MyCommand {
public MyCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mycommand")
                .then(Commands.argument("id", ColorArgument.color())
                        .executes(context -> execute(context, ColorArgument.getColor(context, "id")))));
    }

    private static int execute(CommandContext<CommandSourceStack> context, ChatFormatting id) {
        context.getSource().sendSuccess(() -> Component.literal("Selected color: ").withStyle(id), true);
        return 1;
    }
}
