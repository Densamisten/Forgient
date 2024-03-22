package io.github.densamisten.command.vanilla;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/*
*   Teletransport command
*/
public class TeletransportCommand {
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.teleport.invalidPosition"));

    public TeletransportCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> teletransportNode = dispatcher.register(Commands.literal("teletransport")
                .then(Commands.argument("location", Vec3Argument.vec3())
                        .executes(context -> teleportToPos(context.getSource(), Collections.singleton(context.getSource().getEntityOrException()), context.getSource().getLevel(), Vec3Argument.getCoordinates(context, "location"), WorldCoordinates.current(), null)))
                .then(Commands.argument("destination", EntityArgument.entity())
                        .executes(context -> teleportToEntity(context.getSource(), Collections.singleton(context.getSource().getEntity()), EntityArgument.getEntity(context, "destination"))))
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("location", Vec3Argument.vec3())
                                .executes(context -> teleportToPos(context.getSource(), EntityArgument.getEntities(context, "targets"), context.getSource().getLevel(), Vec3Argument.getCoordinates(context, "location"), null, null)))
                        .then(Commands.argument("rotation", RotationArgument.rotation())
                                .executes(context -> teleportToPos(context.getSource(), EntityArgument.getEntities(context, "targets"), context.getSource().getLevel(), Vec3Argument.getCoordinates(context, "location"), RotationArgument.getRotation(context, "rotation"), null)))
                        .then(Commands.literal("facing")
                                .then(Commands.literal("entity")
                                        .then(Commands.argument("facingEntity", EntityArgument.entity())
                                                .executes(context -> teleportToPos(context.getSource(), EntityArgument.getEntities(context, "targets"), context.getSource().getLevel(), Vec3Argument.getCoordinates(context, "location"), null, new LookAt(EntityArgument.getEntity(context, "facingEntity"), EntityAnchorArgument.Anchor.FEET))))
                                        .then(Commands.argument("facingAnchor", EntityAnchorArgument.anchor())
                                                .executes(context -> teleportToPos(context.getSource(), EntityArgument.getEntities(context, "targets"), context.getSource().getLevel(), Vec3Argument.getCoordinates(context, "location"), null, new LookAt(EntityArgument.getEntity(context, "facingEntity"), EntityAnchorArgument.getAnchor(context, "facingAnchor"))))))
                                .then(Commands.argument("facingLocation", Vec3Argument.vec3())
                                        .executes(context -> teleportToPos(context.getSource(), EntityArgument.getEntities(context, "targets"), context.getSource().getLevel(), Vec3Argument.getCoordinates(context, "location"), null, new LookAt(Vec3Argument.getVec3(context, "facingLocation"))))))));

        // Add synonym for "teleport"
        dispatcher.register(Commands.literal("ttp")
                .redirect(teletransportNode));
    }


    private static int teleportToEntity(CommandSourceStack context, Collection<? extends Entity> entities, Entity entity1) throws CommandSyntaxException {
        for(Entity entity : entities) {
            performTeleport(context, entity, (ServerLevel)entity1.level(), entity1.getX(), entity1.getY(), entity1.getZ(), EnumSet.noneOf(RelativeMovement.class), entity1.getYRot(), entity1.getXRot(), null);
        }

        if (entities.size() == 1) {
            context.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.single", entities.iterator().next().getDisplayName(), entity1.getDisplayName()), true);
        } else {
            context.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.multiple", entities.size(), entity1.getDisplayName()), true);
        }

        return entities.size();
    }
    private static int teleportToPos(CommandSourceStack context, Collection<? extends Entity> p_139027_, ServerLevel level, Coordinates p_139029_, @Nullable Coordinates p_139030_, @Nullable LookAt lookAt) throws CommandSyntaxException {
        Vec3 vec3 = p_139029_.getPosition(context);
        Vec2 vec2 = p_139030_ == null ? null : p_139030_.getRotation(context);
        Set<RelativeMovement> set = EnumSet.noneOf(RelativeMovement.class);
        if (p_139029_.isXRelative()) {
            set.add(RelativeMovement.X);
        }

        if (p_139029_.isYRelative()) {
            set.add(RelativeMovement.Y);
        }

        if (p_139029_.isZRelative()) {
            set.add(RelativeMovement.Z);
        }

        if (p_139030_ == null) {
            set.add(RelativeMovement.X_ROT);
            set.add(RelativeMovement.Y_ROT);
        } else {
            if (p_139030_.isXRelative()) {
                set.add(RelativeMovement.X_ROT);
            }

            if (p_139030_.isYRelative()) {
                set.add(RelativeMovement.Y_ROT);
            }
        }

        for(Entity entity : p_139027_) {
            if (p_139030_ == null) {
                performTeleport(context, entity, level, vec3.x, vec3.y, vec3.z, set, entity.getYRot(), entity.getXRot(), lookAt);
            } else {
                performTeleport(context, entity, level, vec3.x, vec3.y, vec3.z, set, vec2.y, vec2.x, lookAt);
            }
        }

        if (p_139027_.size() == 1) {
            context.sendSuccess(() -> Component.translatable("commands.teleport.success.location.single", p_139027_.iterator().next().getDisplayName(), formatDouble(vec3.x), formatDouble(vec3.y), formatDouble(vec3.z)), true);
        } else {
            context.sendSuccess(() -> Component.translatable("commands.teleport.success.location.multiple", p_139027_.size(), formatDouble(vec3.x), formatDouble(vec3.y), formatDouble(vec3.z)), true);
        }

        return p_139027_.size();
    }

    private static String formatDouble(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private static void performTeleport(CommandSourceStack p_139015_, Entity p_139016_, ServerLevel p_139017_, double p_139018_, double p_139019_, double p_139020_, Set<RelativeMovement> p_139021_, float p_139022_, float p_139023_, @Nullable LookAt lookAt) throws CommandSyntaxException {
        net.minecraftforge.event.entity.EntityTeleportEvent.TeleportCommand event = net.minecraftforge.event.ForgeEventFactory.onEntityTeleportCommand(p_139016_, p_139018_, p_139019_, p_139020_);
        if (event.isCanceled()) return;
        p_139018_ = event.getTargetX(); p_139019_ = event.getTargetY(); p_139020_ = event.getTargetZ();
        BlockPos blockpos = BlockPos.containing(p_139018_, p_139019_, p_139020_);
        if (!Level.isInSpawnableBounds(blockpos)) {
            throw INVALID_POSITION.create();
        } else {
            float f = Mth.wrapDegrees(p_139022_);
            float f1 = Mth.wrapDegrees(p_139023_);
            if (p_139016_.teleportTo(p_139017_, p_139018_, p_139019_, p_139020_, p_139021_, f, f1)) {
                if (lookAt != null) {
                    lookAt.perform(p_139015_, p_139016_);
                }

                label23: {
                    if (p_139016_ instanceof LivingEntity livingentity) {
                        if (livingentity.isFallFlying()) {
                            break label23;
                        }
                    }

                    p_139016_.setDeltaMovement(p_139016_.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
                    p_139016_.setOnGround(true);
                }

                if (p_139016_ instanceof PathfinderMob pathfindermob) {
                    pathfindermob.getNavigation().stop();
                }

            }
        }
    }
    }
    class LookAt {
        private final Vec3 position;
        private final Entity entity;
        private final EntityAnchorArgument.Anchor anchor;

        public LookAt(Entity p_139056_, EntityAnchorArgument.Anchor p_139057_) {
            this.entity = p_139056_;
            this.anchor = p_139057_;
            this.position = p_139057_.apply(p_139056_);
        }

        public LookAt(Vec3 p_139059_) {
            this.entity = null;
            this.position = p_139059_;
            this.anchor = null;
        }

        public void perform(CommandSourceStack p_139061_, Entity p_139062_) {
            if (this.entity != null) {
                if (p_139062_ instanceof ServerPlayer) {
                    ((ServerPlayer)p_139062_).lookAt(p_139061_.getAnchor(), this.entity, this.anchor);
                } else {
                    p_139062_.lookAt(p_139061_.getAnchor(), this.position);
                }
            } else {
                p_139062_.lookAt(p_139061_.getAnchor(), this.position);
            }

        }
    }
