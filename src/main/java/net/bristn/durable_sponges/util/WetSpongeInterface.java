package net.bristn.durable_sponges.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.bristn.durable_sponges.CommonModInitializer;
import net.bristn.durable_sponges.block_states.ModBlockStates;
import net.bristn.durable_sponges.game_rules.ModGameRules;
import net.bristn.durable_sponges.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.TraversalNodeStatus;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class WetSpongeInterface {
    private static int counter = 0;

    private int id;
    private boolean isHeatInitialized;
    private long lastAbsorption;
    private long lastTimeReduction;
    private Set<BlockPos> reachableInfluence;
    private Set<BlockPos> reachableEdgeInfluence;

    public WetSpongeInterface() {
        isHeatInitialized = false;
        lastAbsorption = 0;
        lastTimeReduction = 0;
        reachableInfluence = new HashSet<>();
        reachableEdgeInfluence = new HashSet<>();
        id = counter;
        counter++;
    }

    public int getId() {
        return id;
    }

    public boolean isHeatInitialized() {
        return this.isHeatInitialized;
    }

    public void setIsHeatInitialized(boolean initialized) {
        this.isHeatInitialized = initialized;
    }

    /**
     * Checks all neighbors of the wet sponge position. Sets the heat level state
     * based on the highest heat level found in the adjacent blocks
     * 
     * @param spongePos
     * @param level
     */
    public void updateHeatLevel(BlockPos spongePos, ServerLevel level) {
        var neighbors = List.of(
                spongePos.north(), spongePos.east(), spongePos.south(),
                spongePos.west(), spongePos.above(), spongePos.below());

        var heatLevel = 0;
        for (var neighbor : neighbors) {
            var state = level.getBlockState(neighbor);

            if (state.is(ModBlockTags.HIGH_HEAT)) {
                heatLevel = 3;
                break;
            }

            if (state.is(ModBlockTags.MEDIUM_HEAT)) {
                heatLevel = 2;
                continue;
            }

            if (state.is(ModBlockTags.LOW_HEAT)) {
                heatLevel = Math.max(heatLevel, 1);
                continue;
            }
        }

        var state = level.getBlockState(spongePos);
        var prevHeatLevel = state.getValue(ModBlockStates.HEAT_LEVEL);

        // Store the heat level in the sponge state
        var isFirstUpdate = (this.isHeatInitialized == false && prevHeatLevel == ModBlockStates.HEAT_LEVEL_MAX);
        if (prevHeatLevel != heatLevel || isFirstUpdate) {
            var maxTime = ModGameRules.getAbsorptionTimeMax(level);
            state = state.setValue(ModBlockStates.ABSORPTION_TIME, maxTime);
            state = state.setValue(ModBlockStates.HEAT_LEVEL, heatLevel);

            if (prevHeatLevel != 0) {
                state = state.setValue(ModBlockStates.LAST_HEAT_LEVEL, prevHeatLevel);
            }

            level.setBlockAndUpdate(spongePos, state);
            updateReachable(level, spongePos, Math.max(heatLevel, prevHeatLevel));
            // this.debugVisualizePositions(level);
        }
    }

    public boolean isReachable(BlockPos pos) {
        if (this.reachableInfluence == null) {
            CommonModInitializer.LOGGER.error("The reachable influence has not been initialized");
            return false;
        }

        return this.reachableInfluence.contains(pos);
    }

    public boolean isReachableEdge(BlockPos pos) {
        if (this.reachableEdgeInfluence == null) {
            CommonModInitializer.LOGGER.error("The reachable edge influence has not been initialized");
            return false;
        }

        return this.reachableEdgeInfluence.contains(pos);
    }

    public void updateReachable(ServerLevel level, BlockPos pos, int effectiveHeat) {
        var radius = SpongeUtility.getRadiusFromHeatLevel(level, effectiveHeat);
        if (radius == 0) {
            this.reachableInfluence = new HashSet<>();
            this.reachableEdgeInfluence = new HashSet<>();
            return;
        }

        var reachable = SpongeUtility.getReachableInfluence(pos, level, radius);
        this.reachableInfluence = reachable.all();
        this.reachableEdgeInfluence = reachable.edge();

        // Replace any water blocks in the reachable influence with air
        for (var position : this.reachableInfluence) {
            var isWater = SpongeUtility.isWater(position, level);
            if (isWater == false) {
                continue;
            }

            level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public void debugVisualizePositions(ServerLevel level) {
        if (this.reachableInfluence != null) {
            for (var position : this.reachableInfluence) {
                level.sendParticles(ParticleTypes.END_ROD,
                        position.getX() + 0.5,
                        position.getY() + 0.5,
                        position.getZ() + 0.5,
                        1, 0, 0, 0, 0);
            }
        }

        if (this.reachableEdgeInfluence != null) {
            for (var position : this.reachableEdgeInfluence) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        position.getX() + 0.5,
                        position.getY() + 0.5,
                        position.getZ() + 0.5,
                        1, 0, 0, 0, 0);
            }
        }
    }

    public void setLastAbsorption(long timestamp) {
        this.lastAbsorption = timestamp;
    }

    public long getLastAbsorption() {
        return this.lastAbsorption;
    }

    public void setLastTimeReduction(long timestamp) {
        this.lastTimeReduction = timestamp;
    }

    public long getLastTimeReduction() {
        return this.lastTimeReduction;
    }

}
