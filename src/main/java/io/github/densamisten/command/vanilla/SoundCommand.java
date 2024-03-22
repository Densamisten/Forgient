package io.github.densamisten.command.vanilla;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class SoundCommand {
    private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType(Component.translatable("commands.playsound.failed"));

    public SoundCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> requiredargumentbuilder = Commands.argument("sound", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS);

        for(SoundSource soundsource : SoundSource.values()) {
            requiredargumentbuilder.then(source(soundsource));
        }

        dispatcher.register(Commands.literal("sound").then(requiredargumentbuilder));
    }
    private static LiteralArgumentBuilder<CommandSourceStack> source(SoundSource soundSource) {
        return Commands.literal(soundSource.getName()).then(Commands.argument("targets", EntityArgument.players()).executes((context) -> playSound(context.getSource(), EntityArgument.getPlayers(context, "targets"), ResourceLocationArgument.getId(context, "sound"), soundSource, context.getSource().getPosition(), 1.0F, 1.0F, 0.0F)).then(Commands.argument("pos", Vec3Argument.vec3()).executes((context) -> playSound(context.getSource(), EntityArgument.getPlayers(context, "targets"), ResourceLocationArgument.getId(context, "sound"), soundSource, Vec3Argument.getVec3(context, "pos"), 1.0F, 1.0F, 0.0F)).then(Commands.argument("volume", FloatArgumentType.floatArg(0.0F)).executes((context) -> playSound(context.getSource(), EntityArgument.getPlayers(context, "targets"), ResourceLocationArgument.getId(context, "sound"), soundSource, Vec3Argument.getVec3(context, "pos"), context.getArgument("volume", Float.class), 1.0F, 0.0F)).then(Commands.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F)).executes((context) -> playSound(context.getSource(), EntityArgument.getPlayers(context, "targets"), ResourceLocationArgument.getId(context, "sound"), soundSource, Vec3Argument.getVec3(context, "pos"), context.getArgument("volume", Float.class), context.getArgument("pitch", Float.class), 0.0F)).then(Commands.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((context) -> playSound(context.getSource(), EntityArgument.getPlayers(context, "targets"), ResourceLocationArgument.getId(context, "sound"), soundSource, Vec3Argument.getVec3(context, "pos"), context.getArgument("volume", Float.class), context.getArgument("pitch", Float.class), context.getArgument("minVolume", Float.class))))))));
    }

    private static int playSound(CommandSourceStack source, Collection<ServerPlayer> targets, ResourceLocation sound, SoundSource soundSource, Vec3 pos, float volume, float pitch, float minVolume) throws CommandSyntaxException {
        Holder<SoundEvent> holder = Holder.direct(SoundEvent.createVariableRangeEvent(sound));
        double d0 = Mth.square(holder.value().getRange(volume));
        int i = 0;
        long j = source.getLevel().getRandom().nextLong();

        for (ServerPlayer player : targets) {
            double d1 = pos.x - player.getX();
            double d2 = pos.y - player.getY();
            double d3 = pos.z - player.getZ();
            double d4 = d1 * d1 + d2 * d2 + d3 * d3;
            Vec3 vec3 = pos;
            float f = volume;
            if (d4 > d0) {
                if (minVolume <= 0.0F) {
                    continue;
                }

                double d5 = Math.sqrt(d4);
                vec3 = new Vec3(player.getX() + d1 / d5 * 2.0D, player.getY() + d2 / d5 * 2.0D, player.getZ() + d3 / d5 * 2.0D);
                f = minVolume;
            }

            player.connection.send(new ClientboundSoundPacket(holder, soundSource, vec3.x(), vec3.y(), vec3.z(), f, pitch, j));
            ++i;
        }

        if (i == 0) {
            throw ERROR_TOO_FAR.create();
        } else {
            if (targets.size() == 1) {
                source.sendSuccess(() -> Component.translatable("commands.playsound.success.single", Component.translationArg(sound), targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.playsound.success.multiple", Component.translationArg(sound), targets.size()), true);
            }

            return i;
        }
    }
}
