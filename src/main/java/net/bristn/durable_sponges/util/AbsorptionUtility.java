package net.bristn.durable_sponges.util;

import java.util.HashSet;

import net.bristn.durable_sponges.block_states.ModBlockStates;
import net.bristn.durable_sponges.game_rules.ModGameRules;
import net.bristn.durable_sponges.records.AbsorptionResult;
import net.bristn.durable_sponges.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public class AbsorptionUtility {

    /**
     * Tries to absorb the water from spreading or being placed at all. Updates the
     * first sponge and replaced it with a wet sponge if necessary
     * 
     * @param serverLevel
     * @param waterPos
     * @param placeAirBlock True to replace the water with an air block
     * @return
     */
    public static AbsorptionResult tryAbsorbWater(ServerLevel serverLevel, BlockPos waterPos, boolean placeAirBlock) {
        var affectedWater = new HashSet<BlockPos>();
        affectedWater.add(waterPos);

        var nearbySponges = SpongeRadiusUtility.getClosestSponges(serverLevel, waterPos);
        if (nearbySponges.isEmpty()) {
            return new AbsorptionResult(affectedWater, null, false);
        }

        for (var spongePos : nearbySponges) {
            var result = tryAbsorbWater(serverLevel, waterPos, spongePos, placeAirBlock, false);
            if (result.hasAbsorbed() == true) {
                return result;
            }
        }

        return new AbsorptionResult(affectedWater, null, false);
    }

    /**
     * 
     * @param level
     * @param waterPos
     * @param spongePos
     * @param placeAirBlock       True to replace the water with an air block
     * @param allowWaterPositions True to make the hasAbsorbed return true, if the
     *                            checked position is in the water collection. This
     *                            collection is technically not in the reachable
     *                            range
     * @return
     */
    public static AbsorptionResult tryAbsorbWater(ServerLevel level, BlockPos waterPos, BlockPos spongePos,
            boolean placeAirBlock, boolean allowWaterPositions) {

        var affectedWater = new HashSet<BlockPos>();
        affectedWater.add(waterPos);

        var waterState = level.getBlockState(waterPos);
        var spongeState = level.getBlockState(spongePos);

        // ! Prevent replacing blocks like torches with air. Torches and similar blocks
        // ! do not block the absorption range and may be on the border to the water
        var replaceable = waterState.canBeReplaced(Fluids.WATER) || AbsorptionUtility.isHeatSource(waterPos, level);

        // If this is a regular sponge, replace it with a wet sponge
        if (spongeState.is(Blocks.SPONGE)) {
            replaceWithWetSponge(spongePos, level);
            if (placeAirBlock == true && replaceable == false) {
                level.setBlockAndUpdate(waterPos, Blocks.AIR.defaultBlockState());
            }

            return new AbsorptionResult(affectedWater, spongePos, true);
        }

        if (spongeState.is(Blocks.WET_SPONGE) == false) {
            return new AbsorptionResult(affectedWater, null, false);
        }

        // Check if this water is in range of the sponge
        var access = SpongeTracker.getWetSponge(level, spongePos);
        if (access == null) {
            return new AbsorptionResult(affectedWater, spongePos, false);
        }

        var isAffected = access.isReachable(waterPos) || (allowWaterPositions && access.isWaterPosition(waterPos));
        if (isAffected == false) {
            return new AbsorptionResult(affectedWater, spongePos, false);
        }

        // If this sponge has no time left, ignore it
        var remainingTime = spongeState.getValue(ModBlockStates.ABSORPTION_TIME);
        if (remainingTime <= 0) {
            return new AbsorptionResult(affectedWater, spongePos, false);
        }

        var heatLevel = spongeState.getValue(ModBlockStates.HEAT_LEVEL);
        var lastHeatLevel = spongeState.getValue(ModBlockStates.LAST_HEAT_LEVEL);
        if (heatLevel == 0 && lastHeatLevel == 0) {
            return new AbsorptionResult(affectedWater, spongePos, false);
        }

        // Reduces the internal timer for the absorption. Only reduces if the previous
        // heat level was larger than the current one. This allows buffering the last
        // radius for the absorption duration.
        var now = level.getGameTime();
        var lastTimeReduction = access.getLastTimeReduction();
        var reduceTime = (now - lastTimeReduction) > 20;
        if (reduceTime == true) {
            access.setLastTimeReduction(now);

            if (lastHeatLevel > heatLevel) {
                var newTime = remainingTime - 1;

                // If the previous heating time has run out, reduce the radius & update the
                // influence
                if (newTime <= 0) {
                    newTime = ModGameRules.getAbsorptionTimeMax(level);
                    spongeState = spongeState.setValue(ModBlockStates.LAST_HEAT_LEVEL, heatLevel);
                    access.updateReachable(level, spongePos, heatLevel);
                }

                spongeState = spongeState.setValue(ModBlockStates.ABSORPTION_TIME, newTime);
                level.setBlockAndUpdate(spongePos, spongeState);
            }
        }

        if (placeAirBlock == true && replaceable == false) {
            level.setBlockAndUpdate(waterPos, Blocks.AIR.defaultBlockState());
        }

        access.setLastAbsorption(level.getGameTime());
        return new AbsorptionResult(affectedWater, spongePos, true);
    }

    /**
     * Checks the surrounding blocks. If there is any water in range, the sponge
     * block is replaced with the wet sponge and the according BlockState.
     * The wet sponge immediately checks the heat level and absorbs the water
     * 
     * @param spongePos
     * @param level
     * @return
     */
    public static AbsorptionResult tryFirstAbsorption(BlockPos spongePos, ServerLevel level) {
        var affectedWater = new HashSet<BlockPos>();
        var hasWater = false;
        var maxRange = ModGameRules.getAbsorptionRangeHigh(level);
        var reachableInfluence = SpongeRadiusUtility.getReachableInfluence(spongePos, level, maxRange);
        var reachable = reachableInfluence.all();
        for (var position : reachable) {
            var isWater = isWater(position, level);
            if (isWater == false) {
                continue;
            }

            hasWater = isWater;
            affectedWater.add(position);
        }

        if (hasWater == false) {
            return new AbsorptionResult(affectedWater, spongePos, false);
        }

        replaceWithWetSponge(spongePos, level);
        return new AbsorptionResult(affectedWater, spongePos, true);
    }

    /**
     * Replaces the sponge block with a wet sponge that has the maximum absorption
     * 
     * @param spongePos
     * @param level
     */
    private static void replaceWithWetSponge(BlockPos spongePos, ServerLevel level) {
        var state = Blocks.WET_SPONGE.defaultBlockState();
        var maxTime = ModGameRules.getAbsorptionTimeMax(level);
        state = state.setValue(ModBlockStates.ABSORPTION_TIME, maxTime);
        state = state.setValue(ModBlockStates.LAST_HEAT_LEVEL, ModBlockStates.HEAT_LEVEL_MAX);
        state = state.setValue(ModBlockStates.HEAT_LEVEL, ModBlockStates.HEAT_LEVEL_MAX);
        level.setBlockAndUpdate(spongePos, state);
        level.playSound(null, spongePos, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Utility function to check whether a block is water & is influenced by the
     * sponges absorption
     * 
     * @param pos
     * @param level
     * @return
     */
    public static boolean isWater(BlockPos pos, ServerLevel level) {
        var block = level.getBlockState(pos);
        var fluid = level.getFluidState(pos);

        if (fluid.is(Fluids.WATER) || fluid.is(Fluids.FLOWING_WATER)) {
            return true;
        }

        if (block.is(Blocks.KELP) || block.is(Blocks.KELP_PLANT)) {
            return true;
        }

        if (block.is(Blocks.SEAGRASS) || block.is(Blocks.TALL_SEAGRASS)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the given block is tagged as a heat source
     * 
     * @param pos
     * @param level
     * @return
     */
    public static boolean isHeatSource(BlockPos pos, ServerLevel level) {
        var block = level.getBlockState(pos);

        return block.is(ModBlockTags.HIGH_HEAT)
                || block.is(ModBlockTags.MEDIUM_HEAT)
                || block.is(ModBlockTags.LOW_HEAT);
    }
}
