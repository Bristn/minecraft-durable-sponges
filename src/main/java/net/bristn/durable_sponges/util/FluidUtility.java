package net.bristn.durable_sponges.util;

import net.bristn.durable_sponges.CommonModInitializer;
import net.bristn.durable_sponges.block_states.ModBlockStates;
import net.bristn.durable_sponges.game_rules.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public class FluidUtility {

    /**
     * Tries to absorb the water from spreading or being placed at all. Updates the
     * first sponge and replaced it with a wet sponge if necessary
     * 
     * @param serverLevel
     * @param waterPos
     * @return
     */
    public static boolean tryAbsorbWater(ServerLevel serverLevel, BlockPos waterPos, boolean replaceWithAir) {
        var nearbySponges = SpongeUtility.getClosestSponges(serverLevel, waterPos);
        if (nearbySponges.isEmpty()) {
            return false;
        }

        for (var spongePos : nearbySponges) {
            var spongeState = serverLevel.getBlockState(spongePos);

            // If this is a regular sponge, replace it with a wet sponge
            if (spongeState.is(Blocks.SPONGE)) {
                SpongeUtility.replaceWithWetSponge(spongePos, serverLevel);
                if (replaceWithAir == true) {
                    serverLevel.setBlockAndUpdate(waterPos, Blocks.AIR.defaultBlockState());
                }

                return true;
            }

            // Check if this water is in range of the sponge
            var access = SpongeTracker.getWetSponge(spongePos);
            if (access == null) {
                continue;
            }

            // TODO: Reachable is not initialized soon enough after loading a level!
            var isAffected = access.isReachable(waterPos);
            if (isAffected == false) {
                // CommonModInitializer.LOGGER.error("Is not reachable");
                continue;
            }

            // If this sponge has no time left, ignore it
            var remainingTime = spongeState.getValue(ModBlockStates.ABSORPTION_TIME);
            if (remainingTime <= 0) {
                continue;
            }

            // Reduces the internal timer for the absorption. Only reduces if the previous
            // heat level was larger than the current one. This allows buffering the last
            // radius for the absorption duration.
            var now = serverLevel.getGameTime();
            var lastTimeReduction = access.getLastTimeReduction();
            var reduceTime = (now - lastTimeReduction) > 20;
            if (reduceTime == true) {
                access.setLastTimeReduction(now);

                var heatLevel = spongeState.getValue(ModBlockStates.HEAT_LEVEL);
                var lastHeatLevel = spongeState.getValue(ModBlockStates.LAST_HEAT_LEVEL);
                if (lastHeatLevel > heatLevel) {
                    var newTime = remainingTime - 1;

                    // If the previous heating time has run out, reduce the radius & update the
                    // influence
                    if (newTime <= 0) {
                        newTime = ModGameRules.getAbsorptionTimeMax(serverLevel);
                        spongeState = spongeState.setValue(ModBlockStates.LAST_HEAT_LEVEL, heatLevel);
                        access.updateReachable(serverLevel, spongePos, heatLevel);
                    }

                    spongeState = spongeState.setValue(ModBlockStates.ABSORPTION_TIME, newTime);
                    serverLevel.setBlockAndUpdate(spongePos, spongeState);
                }
            }

            if (replaceWithAir == true) {
                serverLevel.setBlockAndUpdate(waterPos, Blocks.AIR.defaultBlockState());
            }

            access.setLastAbsorption(serverLevel.getGameTime());
            return true;
        }

        return false;
    }
}
