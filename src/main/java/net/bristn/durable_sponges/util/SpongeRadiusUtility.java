package net.bristn.durable_sponges.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.bristn.durable_sponges.game_rules.ModGameRules;
import net.bristn.durable_sponges.poi_types.ModPoiTypes;
import net.bristn.durable_sponges.records.SpongeInfluence;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.BlockPos.TraversalNodeStatus;

public class SpongeRadiusUtility {
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
     * Gets all block positions of water blocks that will be replaced with air. The
     * traversal only continues through water or air blocks
     * 
     * @param spongePos
     * @param level
     * @param radius
     * @return
     */
    public static SpongeInfluence getReachableInfluence(BlockPos spongePos, ServerLevel level, float radius) {
        Set<BlockPos> all = new HashSet<>();
        Set<BlockPos> edge = new HashSet<>();
        Set<BlockPos> water = new HashSet<>();

        var startCenter = spongePos.getCenter();
        var waterRadius = radius + 1;
        var waterRadiusSq = Math.pow(waterRadius, 2);
        var radiusSq = Math.pow(radius, 2);
        var maxDepth = waterRadiusSq * 2;

        BlockPos.breadthFirstTraversal(spongePos, (int) Math.ceil(maxDepth), 1024,
                (pos, consumer) -> acceptAllNeighbors(pos, consumer),
                (pos) -> {
                    // ! Distance check makes influence rounded instead of diamond shaped
                    var distance = pos.getCenter().distanceToSqr(startCenter);
                    if (distance > waterRadiusSq) {
                        return TraversalNodeStatus.SKIP;
                    }

                    if (distance > radiusSq) {
                        water.add(pos);
                        return TraversalNodeStatus.SKIP;
                    }

                    if (pos.equals(spongePos)) {
                        return TraversalNodeStatus.ACCEPT;
                    }

                    // Absorption is allowed through air blocks
                    var state = level.getBlockState(pos);
                    if (state.isAir()) {
                        all.add(pos);
                        return TraversalNodeStatus.ACCEPT;
                    }

                    // Absorption is allowed through water blocks
                    var isWater = AbsorptionUtility.isWater(pos, level);
                    if (isWater) {
                        all.add(pos);
                        return TraversalNodeStatus.ACCEPT;
                    }

                    // Absorption is allowed through any block that is replaceable by water
                    var replaceable = state.canBeReplaced(Fluids.WATER);
                    if (replaceable) {
                        all.add(pos);
                        return TraversalNodeStatus.ACCEPT;
                    }

                    all.add(pos);
                    return TraversalNodeStatus.SKIP;
                });

        return new SpongeInfluence(all, edge, water);
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

}
