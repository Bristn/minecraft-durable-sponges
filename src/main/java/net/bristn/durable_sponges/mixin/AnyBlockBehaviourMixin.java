package net.bristn.durable_sponges.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.bristn.durable_sponges.util.SpongePlacementUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockBehaviour.class)
public abstract class AnyBlockBehaviourMixin {

    @Inject(method = "onPlace", at = @At("TAIL"), cancellable = true)
    private void onPlaceAnyBlock(final BlockState state, final Level level, final BlockPos pos,
            final BlockState oldState, final boolean movedByPiston, CallbackInfo callback) {

        // TODO: Having torch on block does currently not work, as water washes away the
        // torch

        // ! Easy fix by not letting min distance be 1

        SpongePlacementUtility.onPlaceAnyBlock(state, level, pos, oldState);
    }
}
