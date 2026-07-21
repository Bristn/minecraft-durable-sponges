package net.bristn.durable_sponges.util;

import java.util.List;

import net.bristn.durable_sponges.CommonModInitializer;
import net.bristn.durable_sponges.block_states.ModBlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpongePlacementUtility {
    public static void onPlaceAnyBlock(BlockState state, Level level, BlockPos pos, BlockState oldState) {
        if (level.isClientSide()) {
            return;
        }

        if (oldState.is(state.getBlock())) {
            return;
        }

        // Handle sponge moving in other class
        if (state.is(Blocks.SPONGE) || state.is(Blocks.WET_SPONGE)) {
            return;
        }

        // Prevent the sponge absorption from repeatedly triggering influence updates
        if (state.isAir() && oldState.is(Blocks.WATER)) {
            return;
        }

        var serverLevel = (ServerLevel) level;
        var nearbySponges = SpongeUtility.getClosestSponges(serverLevel, pos);
        updateWithBlockPlacement(serverLevel, nearbySponges, pos);
    }

    private static void updateWithBlockPlacement(ServerLevel level, List<BlockPos> nearbySponges, BlockPos position) {
        for (var spongePos : nearbySponges) {
            var state = level.getBlockState(spongePos);
            if (state.is(Blocks.SPONGE) == true) {
                var success = SpongeUtility.tryFirstAbsorption(spongePos, level);
                if (success == true) {
                    break;
                }

                continue;
            }

            if (state.is(Blocks.WET_SPONGE) == false) {
                continue;
            }

            // If the block has changed within the reachable area, recalculate the influence
            // of the sponge
            var access = SpongeTracker.getWetSponge(spongePos);
            if (access == null) {
                CommonModInitializer.LOGGER.error("Sponge access is null");
                continue;
            }

            var relevant = access.isReachableEdge(position) || access.isReachable(position);
            if (relevant == false) {
                continue;
            }

            // If the air block is placed at the reachable edge, update the positions
            var heatLevel = state.getValue(ModBlockStates.HEAT_LEVEL);
            var lastHeatLevel = state.getValue(ModBlockStates.LAST_HEAT_LEVEL);
            access.updateReachable(level, spongePos, Math.max(heatLevel, lastHeatLevel));
        }
    }
}
