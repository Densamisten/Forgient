package io.github.densamisten.item;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ForgientKey extends ComplexItem {
    public ForgientKey(Properties p_40743_) {
        super(p_40743_);
    }
    @Override
    public Packet<?> getUpdatePacket(ItemStack itemStack, Level level, Player player) {
        return null;
    }
}
