package net.bristn.durable_sponges.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.bristn.durable_sponges.util.SpongePlacementUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(FallingBlock.class)
public abstract class FallingBlockMixin {

    @Inject(method = "onPlace", at = @At("TAIL"), cancellable = true)
    private void onPlaceAnyBlock(final BlockState state, final Level level, final BlockPos pos,
            final BlockState oldState, final boolean movedByPiston, CallbackInfo callback) {

        SpongePlacementUtility.onPlaceAnyBlock(state, level, pos, oldState);
    }
}
