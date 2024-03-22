package io.github.densamisten.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.item.EmptyMapItem.class)
public class EmptyMapItem {
    @Inject(at = @At("RETURN"), method = "use")
    public void onUse(Level p_41145_, Player p_41146_, InteractionHand p_41147_, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            System.out.println("A map has been printed");
    }
}
