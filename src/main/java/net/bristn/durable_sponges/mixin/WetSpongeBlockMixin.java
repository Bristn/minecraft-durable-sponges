package net.bristn.durable_sponges.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.bristn.durable_sponges.block_states.ModBlockStates;
import net.bristn.durable_sponges.util.SpongeTracker;
import net.bristn.durable_sponges.util.WetSpongeInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

@Mixin(WetSpongeBlock.class)
public abstract class WetSpongeBlockMixin extends Block {

    public WetSpongeBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ModBlockStates.ABSORPTION_TIME);
        builder.add(ModBlockStates.HEAT_LEVEL);
        builder.add(ModBlockStates.LAST_HEAT_LEVEL);
    }

    /**
     * Enables random ticking for the wet sponge block. Without this, the randomTick
     * injection does not work
     * 
     * @param properties
     * @return
     */
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V"))
    private static BlockBehaviour.Properties enableRandomTick(BlockBehaviour.Properties properties) {
        return properties.randomTicks();
    }

    /**
     * When placing the wet sponge block, update its region of influence, but don't
     * absorb any fluid yet
     * 
     * @param state
     * @param level
     * @param spongePos
     * @param oldState
     * @param movedByPiston
     * @param callback
     */
    @Inject(method = "onPlace", at = @At("TAIL"))
    private void onPlaceWetSpongeBlock(final BlockState state, final Level level, final BlockPos spongePos,
            final BlockState oldState, final boolean movedByPiston, CallbackInfo callback) {

        if (level.isClientSide()) {
            return;
        }

        var serverLevel = (ServerLevel) level;
        if (SpongeTracker.hasSponge(serverLevel, spongePos)) {
            return;
        }

        SpongeTracker.addSponge(serverLevel, spongePos, new WetSpongeInterface());

        // Update the heat level with a delay of one tick. This ensure the POI is
        // registered properly
        serverLevel.scheduleTick(spongePos, state.getBlock(), 1);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos spongePos, BlockState state) {
        super.destroy(level, spongePos, state);

        if (level.isClientSide()) {
            return;
        }

        SpongeTracker.removeSponge((ServerLevel) level, spongePos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos spongePos, RandomSource random) {
        super.tick(state, level, spongePos, random);

        if (level.isClientSide()) {
            return;
        }

        var access = SpongeTracker.getWetSponge(level, spongePos);
        if (access == null) {
            return;
        }

        if (access.isHeatInitialized() == false) {
            access.updateHeatLevel(spongePos, level, false);
            access.setIsHeatInitialized(true);
        }
    }
}
