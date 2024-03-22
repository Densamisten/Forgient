package io.github.densamisten.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

 /**
 *
 * Firework command for Firework Mod
 * License: Free for all use.
 * **/

public class FireworkCommand {

    public FireworkCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("firework")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> execute(context, BlockPosArgument.getLoadedBlockPos(context, "pos"), 1, 0xFFFFFF, 1,1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                .then(Commands.argument("shape", IntegerArgumentType.integer(0, 2))
                                        .then(Commands.argument("colors", IntegerArgumentType.integer(0, 16777215))
                                                .then(Commands.argument("flight", IntegerArgumentType.integer(1, 3))
                                                        .executes(context -> execute(context, BlockPosArgument.getLoadedBlockPos(context, "pos"), IntegerArgumentType.getInteger(context, "count"), IntegerArgumentType.getInteger(context, "shape"), IntegerArgumentType.getInteger(context, "colors"), IntegerArgumentType.getInteger(context, "flight")))
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("entity")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("shape", IntegerArgumentType.integer(0, 2))
                                                .then(Commands.argument("colors", IntegerArgumentType.integer(0, 16777215))
                                                        .then(Commands.argument("flight", IntegerArgumentType.integer(1, 3))
                                                                .executes(context -> executeFromEntities(context, EntityArgument.getEntities(context, "targets"), IntegerArgumentType.getInteger(context, "count"), IntegerArgumentType.getInteger(context, "shape"), IntegerArgumentType.getInteger(context, "colors"), IntegerArgumentType.getInteger(context, "flight")))
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context, BlockPos pos, int count, int shape, int colors, int flight) {
        Level level = context.getSource().getLevel();
        for (int i = 0; i < count; i++) {
            double offsetX = (new Random().nextDouble() - 0.5) * 5.0;
            double offsetY = (new Random().nextDouble() - 0.5) * 5.0;
            double offsetZ = (new Random().nextDouble() - 0.5) * 5.0;

            ItemStack fireworkStar = createFireworkStar(shape, colors, flight);

            // Create a new instance of FireworkRocketEntity
            FireworkRocketEntity firework = new FireworkRocketEntity(level, pos.getX() + 0.5 + offsetX, pos.getY() + 0.5 + offsetY, pos.getZ() + 0.5 + offsetZ, fireworkStar);

            // Apply mixin logic here if needed
            firework.setDeltaMovement(firework.getDeltaMovement().add(0.0D, 0.05D, 0.0D));

            level.addFreshEntity(firework);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int executeFromEntities(CommandContext<CommandSourceStack> context, Iterable<? extends Entity> targets, int count, int shape, int colors, int flight) {
        Level level = context.getSource().getLevel();
        for (Entity target : targets) {
            for (int i = 0; i < count; i++) {
                double offsetX = (new Random().nextDouble() - 0.5) * 5.0;
                double offsetY = (new Random().nextDouble() - 0.5) * 5.0;
                double offsetZ = (new Random().nextDouble() - 0.5) * 5.0;

                ItemStack fireworkStar = createFireworkStar(shape, colors, flight);
                FireworkRocketEntity firework = new FireworkRocketEntity(level, target.getX() + 0.5 + offsetX, target.getY() + target.getBbHeight() + 0.5 + offsetY, target.getZ() + 0.5 + offsetZ, fireworkStar);
                level.addFreshEntity(firework);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static ItemStack createFireworkStar(int shape, int colors, int flight) {
        ItemStack fireworkStar = new ItemStack(Items.FIREWORK_ROCKET.asItem());
        CompoundTag fireworksNBT = new CompoundTag();
        ListTag explosionsList = new ListTag();

        CompoundTag explosionTag = getCompoundTag(shape);
        explosionTag.putIntArray("Colors", new int[]{colors}); // Set the desired color
        explosionTag.putIntArray("FadeColors", new int[]{0xFFFFFF}); // White fade color

        explosionsList.add(explosionTag);
        fireworksNBT.put("Explosions", explosionsList);

        fireworkStar.addTagElement("Fireworks", fireworksNBT);

        // Set the flight duration
        CompoundTag fireworkItemNBT = fireworkStar.getOrCreateTagElement("Fireworks");
        fireworkItemNBT.putInt("Flight", flight);

        return fireworkStar;
    }

    @NotNull
    private static CompoundTag getCompoundTag(int shape) {
        CompoundTag explosionTag = new CompoundTag();

        switch (shape) {
            // Small ball explosion
            case 1 -> explosionTag.putInt("Type", 1); // Large ball explosion
            case 2 -> explosionTag.putInt("Type", 2); // Star-shaped explosion

            // Add more shapes as needed

            default -> explosionTag.putInt("Type", 0); // Default to small ball explosion
        }

        explosionTag.putBoolean("Flicker", true); // Flickering effect
        explosionTag.putBoolean("Trail", true); // Trail effect
        return explosionTag;
    }
}