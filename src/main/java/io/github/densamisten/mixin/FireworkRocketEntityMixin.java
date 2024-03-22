package io.github.densamisten.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

/**
 * Firework rocket entity mixin for Firework Mod
 * License: Free for all use.
 **/

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends Entity {

    protected FireworkRocketEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void allowPassingThroughBlocks(CallbackInfo ci) {
        // Get the rocket's current position and motion
        Vec3 currentPosition = this.position();
        Vec3 motion = this.getDeltaMovement();

        // Calculate the next position after applying the motion
        Vec3 nextPosition = currentPosition.add(motion);

        // Check if the rocket's movement would result in a collision with a block
        HitResult hitResult = this.level().clip(new ClipContext(currentPosition, nextPosition, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        // If the rocket is colliding with a block, cancel the collision and move the rocket
        if (hitResult.getType() != HitResult.Type.MISS) {
            ci.cancel();
            this.setPos(nextPosition.x, nextPosition.y, nextPosition.z);
        }
    }
}