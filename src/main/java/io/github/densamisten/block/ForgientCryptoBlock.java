package io.github.densamisten.block;

import io.github.densamisten.gui.ForgientMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ForgientCryptoBlock extends Block {

    public ForgientCryptoBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
        public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
            if (!world.isClientSide()) {
                ForgientMenus.openMenu(player);
            }
            return InteractionResult.SUCCESS;
        }
    }
