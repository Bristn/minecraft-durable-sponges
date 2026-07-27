package net.bristn.durable_sponges.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.bristn.durable_sponges.block_states.ModBlockStates;
import net.bristn.durable_sponges.game_rules.ModGameRules;
import net.bristn.durable_sponges.util.SpongeTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;

@Mixin(BlockBehaviour.class)
public abstract class WetSpongeBehaviourMixin {

    /**
     * Update the heat level of the wet sponge, if its neighbors changed
     * 
     * @param state
     * @param level
     * @param spongePos
     * @param block
     * @param orientation
     * @param movedByPiston
     * @param method
     */
    @Inject(method = "neighborChanged", at = @At("HEAD"))
    private void onWetSpongeNeighborChanged(BlockState state, Level level, BlockPos spongePos, Block block,
            Orientation orientation, boolean movedByPiston, CallbackInfo method) {

        if (level.isClientSide()) {
            return;
        }

        if (state.is(Blocks.WET_SPONGE) == false) {
            return;
        }

        var access = SpongeTracker.getWetSponge((ServerLevel) level, spongePos);
        if (access == null) {
            return;
        }

        access.updateHeatLevel(spongePos, (ServerLevel) level, false);
    }

    /**
     * On random ticks, replaces the wet sponge with a dry sponge if there is a
     * maximum heat source nearby
     * 
     * @param stateParam
     * @param serverLevel
     * @param spongePos
     * @param random
     * @param method
     */
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void randomWetSpongeTick(final BlockState stateParam, final ServerLevel serverLevel,
            final BlockPos spongePos, final RandomSource random, CallbackInfo method) {

        if (serverLevel.isClientSide()) {
            return;
        }

        var state = serverLevel.getBlockState(spongePos);
        if (state.is(Blocks.WET_SPONGE) == false) {
            return;
        }

        var access = SpongeTracker.getWetSponge(serverLevel, spongePos);
        if (access == null) {
            return;
        }

        // Only dry the sponge if it is on the max heat level
        var heatLevel = state.getValue(ModBlockStates.HEAT_LEVEL);
        if (heatLevel != ModBlockStates.HEAT_LEVEL_MAX) {
            return;
        }

        // Check the game rule if drying the sponge is enabled
        var delayBeforeDrying = ModGameRules.getDelayBeforeDrying(serverLevel);
        if (delayBeforeDrying == 0) {
            return;
        }

        // Prevent converting the wet sponge, if it recently absorbed any fluid
        var timeSinceAbsorption = serverLevel.getGameTime() - access.getLastAbsorption();
        if (timeSinceAbsorption <= delayBeforeDrying * 20) {
            return;
        }

        serverLevel.setBlockAndUpdate(spongePos, Blocks.SPONGE.defaultBlockState());
        serverLevel.levelEvent(LevelEvent.PARTICLES_WATER_EVAPORATING, spongePos, 0);
        serverLevel.playSound(null, spongePos, SoundEvents.WET_SPONGE_DRIES, SoundSource.BLOCKS, 1.0F,
                (1.0F + serverLevel.getRandom().nextFloat() * 0.2F) * 0.7F);

        SpongeTracker.removeSponge(serverLevel, spongePos);
    }
}
