package net.bristn.durable_sponges.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.bristn.durable_sponges.CommonModInitializer;
import net.bristn.durable_sponges.block_states.ModBlockStates;
import net.bristn.durable_sponges.game_rules.ModGameRules;
import net.bristn.durable_sponges.poi_types.ModPoiTypes;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys.Server;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.core.BlockPos.TraversalNodeStatus;

public class SpongeUtility {
    private static final Direction[] ALL_DIRECTIONS = Direction.values();

    public static float getRadiusFromHeatLevel(ServerLevel level, int heatLevel) {
        switch (heatLevel) {
            case 1:
                return ModGameRules.getAbsorptionRangeLow(level);

            case 2:
                return ModGameRules.getAbsorptionRangeMedium(level);

            case 3:
                return ModGameRules.getAbsorptionRangeHigh(level);
        }

        return 0;
    }

    /**
     * Utility function to get the closest sponges to the origin position using the
     * poi system. Gets all sponges assuming the maximum range
     * 
     * @param level
     * @param position
     * @return
     */
    public static List<BlockPos> getClosestSponges(ServerLevel level, BlockPos position) {
        var serverLevel = (ServerLevel) level;
        var poiManager = serverLevel.getPoiManager();
        var distance = ModGameRules.getAbsorptionRangeHigh(serverLevel) + 1;

        // Ensure there is only one operation on the stream
        var stream = poiManager.findAll(
                holder -> ModPoiTypes.isSpongePoiType(holder),
                t -> true,
                position,
                (int) Math.ceil(distance),
                PoiManager.Occupancy.ANY);

        return stream.toList();
    }

    /**
     * Utility function to check whether a block is water & is influenced by the
     * sponges absorption
     * 
     * @param pos
     * @param level
     * @return
     */
    public static boolean isWater(BlockPos pos, Level level) {
        var state = level.getBlockState(pos);
        var block = level.getBlockState(pos).getBlock();

        if (block instanceof LiquidBlock) {
            return true;
        }

        if (state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT)) {
            return true;
        }

        if (state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS)) {
            return true;
        }

        return false;
    }

    /**
     * Gets all block positions within the spherical influence without validating
     * any block types
     * 
     * @param spongePos
     * @return
     */
    public static SpongeInfluence getAllInfluence(BlockPos spongePos, float radius) {
        Set<BlockPos> all = new HashSet<>();
        Set<BlockPos> edge = new HashSet<>();

        var startCenter = spongePos.getCenter();
        var radiusSq = Math.pow(radius, 2);
        var radiusEdgeSq = Math.pow(radius - 1, 2);
        var maxDepth = radius * 2;

        BlockPos.breadthFirstTraversal(spongePos, (int) Math.ceil(maxDepth), 1024,
                (pos, consumer) -> acceptAllNeighbors(pos, consumer),
                (pos) -> {
                    var distance = pos.getCenter().distanceToSqr(startCenter);
                    if (distance > radiusSq) {
                        return TraversalNodeStatus.SKIP;
                    }

                    if (distance > radiusEdgeSq) {
                        edge.add(pos);
                    }

                    all.add(pos);
                    return TraversalNodeStatus.ACCEPT;
                });

        return new SpongeInfluence(all, edge);
    }

    /**
     * Gets all block positions of water blocks that will be replaced with air. The
     * traversal only continues through water or air blocks
     * 
     * @param spongePos
     * @param level
     * @return
     */
    public static SpongeInfluence getReachableInfluence(BlockPos spongePos, Level level, float radius) {
        Set<BlockPos> all = new HashSet<>();
        Set<BlockPos> edge = new HashSet<>();

        var startCenter = spongePos.getCenter();
        var radiusSq = Math.pow(radius, 2);
        var maxDepth = radius * 2;

        BlockPos.breadthFirstTraversal(spongePos, (int) Math.ceil(maxDepth), 1024,
                (pos, consumer) -> acceptAllNeighbors(pos, consumer),
                (pos) -> {
                    // ! Distance check makes influence rounded instead of diamond shaped
                    var distance = pos.getCenter().distanceToSqr(startCenter);
                    if (distance > radiusSq) {
                        return TraversalNodeStatus.SKIP;
                    }

                    if (pos.equals(spongePos)) {
                        return TraversalNodeStatus.ACCEPT;
                    }

                    // Ignore the position if it is neither air nor water
                    var state = level.getBlockState(pos);
                    var fluid = level.getFluidState(pos);

                    if (state.isAir() == false && fluid.is(FluidTags.WATER) == false) {
                        edge.add(pos);
                        return TraversalNodeStatus.SKIP;
                    }

                    all.add(pos);
                    return TraversalNodeStatus.ACCEPT;
                });

        return new SpongeInfluence(all, edge);
    }

    // TODO: Calling tick on these positions works when removing the sponge

    public static Set<BlockPos> getWaterPositions(BlockPos spongePos, ServerLevel level) {
        Set<BlockPos> all = new HashSet<>();

        var radius = ModGameRules.getAbsorptionRangeHigh(level) + 1;
        var startCenter = spongePos.getCenter();
        var radiusSq = Math.pow(radius, 2);
        var maxDepth = radius * 2;

        CommonModInitializer.LOGGER.error("Get Test positions");

        BlockPos.breadthFirstTraversal(spongePos, (int) Math.ceil(maxDepth), 1024,
                (pos, consumer) -> acceptAllNeighbors(pos, consumer),
                (pos) -> {
                    // ! Distance check makes influence rounded instead of diamond shaped
                    var distance = pos.getCenter().distanceToSqr(startCenter);
                    if (distance > radiusSq) {
                        return TraversalNodeStatus.SKIP;
                    }

                    if (pos.equals(spongePos)) {
                        return TraversalNodeStatus.ACCEPT;
                    }

                    // Ignore the position if it is neither air nor water
                    var fluid = level.getFluidState(pos);
                    if (fluid.is(FluidTags.WATER) == false) {
                        return TraversalNodeStatus.ACCEPT;
                    }

                    all.add(pos);
                    return TraversalNodeStatus.ACCEPT;
                });

        return all;
    }

    /**
     * Utility that accepts all neighbors for the breadth first search
     * 
     * @param pos
     * @param consumer
     */
    private static void acceptAllNeighbors(BlockPos pos, Consumer<BlockPos> consumer) {
        for (var direction : ALL_DIRECTIONS) {
            var neighbor = pos.relative(direction);
            consumer.accept(neighbor);
        }
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
    public static boolean tryFirstAbsorption(BlockPos spongePos, ServerLevel level) {
        var hasWater = false;
        var maxRange = ModGameRules.getAbsorptionRangeHigh(level);
        var reachableInfluence = SpongeUtility.getReachableInfluence(spongePos, level, maxRange);
        var reachable = reachableInfluence.all();
        for (var position : reachable) {
            var isWater = SpongeUtility.isWater(position, level);
            if (isWater == false) {
                continue;
            }

            hasWater = isWater;
        }

        if (hasWater == false) {
            return false;
        }

        SpongeUtility.replaceWithWetSponge(spongePos, level);
        return true;
    }

    /**
     * Replaces the sponge block with a wet sponge that has the maximum absorption
     * 
     * @param spongePos
     * @param level
     */
    public static void replaceWithWetSponge(BlockPos spongePos, ServerLevel level) {
        var state = Blocks.WET_SPONGE.defaultBlockState();
        var maxTime = ModGameRules.getAbsorptionTimeMax(level);
        state = state.setValue(ModBlockStates.ABSORPTION_TIME, maxTime);
        state = state.setValue(ModBlockStates.LAST_HEAT_LEVEL, ModBlockStates.HEAT_LEVEL_MAX);
        state = state.setValue(ModBlockStates.HEAT_LEVEL, ModBlockStates.HEAT_LEVEL_MAX);
        level.setBlockAndUpdate(spongePos, state);
        level.playSound(null, spongePos, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
