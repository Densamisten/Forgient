package io.github.densamisten.client;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.WindCharge;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

public class ProjectileManager {
    public static void spawnWindChargeAbovePlayer(Level level, LivingEntity player) {
        // Calculate the position above the player
        double posX = player.getX();
        double posY = player.getY() + player.getEyeHeight();
        double posZ = player.getZ();

        // Create a new WindCharge entity at the calculated position
        WindCharge windCharge = new WindCharge(EntityType.WIND_CHARGE, level);
        windCharge.setPos(posX, posY, posZ);

        // Set the motion of the WindCharge entity to move upwards
        windCharge.setDeltaMovement(0, 1, 0);

        // Set the speed of the WindCharge entity

        // Spawn the WindCharge entity in the world
        level.addFreshEntity(windCharge);
    }
}
