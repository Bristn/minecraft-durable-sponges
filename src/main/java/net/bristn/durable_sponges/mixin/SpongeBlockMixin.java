package net.bristn.durable_sponges.mixin;

import net.bristn.durable_sponges.util.AbsorptionUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpongeBlock.class)
public abstract class SpongeBlockMixin extends Block {

    public SpongeBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    /**
     * When placing the sponge block, check for nearby water. If any is found,
     * replace the sponge with a wet sponge which triggers absorption
     * 
     * @param state
     * @param level
     * @param spongePos
     * @param oldState
     * @param movedByPiston
     * @param callback
     */
    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    private void onPlaceSpongeBlock(final BlockState state, final Level level, final BlockPos spongePos,
            final BlockState oldState, final boolean movedByPiston, CallbackInfo callback) {

        if (level.isClientSide()) {
            return;
        }

        var serverLevel = (ServerLevel) level;
        AbsorptionUtility.tryFirstAbsorption(spongePos, serverLevel);
    }

    /**
     * Cancel the default tryAbsorbWater method
     * 
     * @param level
     * @param pos
     * @param callback
     */
    @Inject(method = "tryAbsorbWater", at = @At("HEAD"), cancellable = true)
    protected void durableSPongeTryAbsorbWater(final Level level, final BlockPos pos, CallbackInfo callback) {
        callback.cancel();
    }
}