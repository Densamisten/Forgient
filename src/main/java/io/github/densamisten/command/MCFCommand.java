package io.github.densamisten.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.server.ServerLifecycleHooks;

public class MCFCommand {

    public MCFCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mcf")
                .then(Commands.argument("target", StringArgumentType.word())
                        .then(Commands.argument("source", StringArgumentType.word())
                                .executes(MCFCommand::execute))));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "target");
        ServerPlayer target = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (target != null) {
            String playerUUID = target.getUUID().toString();
            String source = StringArgumentType.getString(context, "source");
            String ipAddress;

            if (source.equalsIgnoreCase("client")) {
                // Get client's IP address
                ipAddress = target.connection.getConnection().getRemoteAddress().toString();
            } else if (source.equalsIgnoreCase("server")) {
                // Get server's IP address and port
                ipAddress = String.valueOf(ServerLifecycleHooks.getCurrentServer().getPort());


                // Construct the message with biome information
                MutableComponent message = Component.literal("Player: ")
                        .append(Component.literal(playerName)
                                .withStyle(style -> style.withColor(ChatFormatting.WHITE)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, playerName))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy " + playerName))))
                                .append("\nUUID: ")
                                .append(Component.literal(playerUUID)
                                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, playerUUID))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy " + playerUUID))))
                                        .append("\nIP Address: ")
                                        .append(Component.literal(ipAddress)
                                                .withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA)
                                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ipAddress))
                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy " + ipAddress)))))));

                context.getSource().sendSuccess(() -> message, true);
                return 1;
            } else {
                // Invalid source argument
                context.getSource().sendFailure(Component.literal("Invalid source argument. Use 'client' or 'server'."));
                return 0;
            }
        } else {
            context.getSource().sendFailure(Component.literal("Player not found!"));
            return 0;
        }
        return 1;
    }

}
