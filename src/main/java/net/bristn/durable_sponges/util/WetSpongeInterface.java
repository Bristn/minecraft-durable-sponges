package net.bristn.durable_sponges.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.bristn.durable_sponges.block_states.ModBlockStates;
import net.bristn.durable_sponges.game_rules.ModGameRules;
import net.bristn.durable_sponges.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class WetSpongeInterface {
    private static boolean enableDebugVisuals = false;
    private static int counter = 0;

    private int id;
    private boolean isHeatInitialized;
    private long lastAbsorption;
    private long lastTimeReduction;
    private Set<BlockPos> reachableInfluence;
    private Set<BlockPos> reachableEdgeInfluence;
    private Set<BlockPos> waterPositions;

    public WetSpongeInterface() {
        isHeatInitialized = false;
        lastAbsorption = 0;
        lastTimeReduction = 0;

        reachableInfluence = ConcurrentHashMap.newKeySet();
        reachableEdgeInfluence = ConcurrentHashMap.newKeySet();
        waterPositions = ConcurrentHashMap.newKeySet();

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
     * @param initializeWaterPositions True to initialize the water positions of the
     *                                 sponge from the breadth first search
     */
    public void updateHeatLevel(BlockPos spongePos, ServerLevel level, boolean initializeWaterPositions) {
        var neighbors = List.of(
                spongePos.north(), spongePos.east(), spongePos.south(),
                spongePos.west(), spongePos.above(), spongePos.below() //
        );

        // Get the highest heat level of any neighbor
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

            var effectiveHeat = Math.max(heatLevel, prevHeatLevel);
            updateReachable(level, spongePos, effectiveHeat, initializeWaterPositions);
        }
    }

    /**
     * Keeps track of the water positions at the edge of the influence. These
     * positions are used to keep track of the absorption time by checking whether
     * there is still water within the sponges radius
     * 
     * @param waterPos
     * @param level
     */
    public void addWaterPosition(BlockPos waterPos, ServerLevel level) {
        // ! Copy the BlockPos, as using the raw value causes by reference issues
        var copy = new BlockPos(waterPos.getX(), waterPos.getY(), waterPos.getZ());
        this.waterPositions.add(copy);
        this.visualizeWaterPositions(level);
    }

    public void updateSpongeUsingWaterPosition(BlockPos spongePos, ServerLevel level) {
        if (this.waterPositions.isEmpty()) {
            return;
        }

        var remove = new ArrayList<BlockPos>();

        for (var waterPos : this.waterPositions) {
            var isWater = AbsorptionUtility.isWater(waterPos, level);
            if (isWater == false) {
                remove.add(waterPos);
                continue;
            }

            // Assumes that this water position is only present, if the sponge is able to
            // reach this water. Therefore update the absorption time of the sponge
            var result = AbsorptionUtility.tryAbsorbWater(level, waterPos, spongePos, false, true);
            if (result.hasAbsorbed() == true) {
                continue;
            }

            // If the water is not in range anymore, tick the water & remove it from the
            // collection
            var fluid = level.getFluidState(waterPos);
            var block = level.getBlockState(waterPos);
            fluid.tick(level, waterPos, block);
            remove.add(waterPos);
        }

        this.waterPositions.removeAll(remove);
    }

    /**
     * Creates a copy of the water positions. Then clears the original collection
     * before calling the tick function
     * 
     * @param spongePos
     * @param level
     */
    public void tickAndClearWaterPositions(BlockPos spongePos, ServerLevel level) {
        var copy = new HashSet<BlockPos>();
        for (var blockPos : this.waterPositions) {
            copy.add(blockPos);
        }

        this.clearWaterPositions();

        for (var blockPos : copy) {
            var isWater = AbsorptionUtility.isWater(blockPos, level);
            if (isWater == false) {
                continue;
            }

            var state = level.getFluidState(blockPos);
            state.tick(level, blockPos, level.getBlockState(blockPos));
        }
    }

    public void clearWaterPositions() {
        this.waterPositions.clear();
    }

    public boolean isReachable(BlockPos pos) {
        return this.reachableInfluence.contains(pos);
    }

    public boolean isReachableEdge(BlockPos pos) {
        return this.reachableEdgeInfluence.contains(pos);
    }

    public boolean isWaterPosition(BlockPos pos) {
        return this.waterPositions.contains(pos);
    }

    public void updateReachable(ServerLevel level, BlockPos pos, int effectiveHeat) {
        this.updateReachable(level, pos, effectiveHeat, false);
    }

    public void updateReachable(ServerLevel level, BlockPos pos, int effectiveHeat, boolean initializeWaterPositions) {
        var radius = SpongeRadiusUtility.getRadiusFromHeatLevel(level, effectiveHeat);
        if (radius == 0) {
            this.reachableInfluence.clear();
            this.reachableEdgeInfluence.clear();
            return;
        }

        var reachable = SpongeRadiusUtility.getReachableInfluence(pos, level, radius);
        this.reachableInfluence.clear();
        this.reachableInfluence.addAll(reachable.all());
        this.reachableEdgeInfluence.clear();
        this.reachableEdgeInfluence.addAll(reachable.edge());

        // Initialize the water positions. This leads to them being ticked and updating
        // the absorption state
        if (initializeWaterPositions == true) {
            this.waterPositions.clear();
            this.waterPositions.addAll(reachable.water());
        }

        // Replace any water blocks in the reachable influence with air
        for (var position : this.reachableInfluence) {
            var isWater = AbsorptionUtility.isWater(position, level);
            if (isWater == false) {
                continue;
            }

            level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        this.tickAndClearWaterPositions(pos, level);
        this.visualizeInfluence(level);
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

    /**
     * Draws particle effects on all reachable positions. Uses a different particle
     * for the edge of influence
     * 
     * @param level
     */
    public void visualizeInfluence(ServerLevel level) {
        if (enableDebugVisuals == false) {
            return;
        }

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

    /**
     * Spawns particle effects on all registered water positions
     * 
     * @param level
     */
    public void visualizeWaterPositions(ServerLevel level) {
        if (enableDebugVisuals == false) {
            return;
        }

        if (this.waterPositions != null) {
            for (var position : this.waterPositions) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        position.getX() + 0.5,
                        position.getY() + 0.5,
                        position.getZ() + 0.5,
                        1, 0, 0, 0, 0);
            }
        }
    }
}
