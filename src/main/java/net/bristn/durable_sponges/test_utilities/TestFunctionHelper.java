package net.bristn.durable_sponges.test_utilities;

import java.util.ArrayList;
import java.util.List;

import net.bristn.durable_sponges.game_rules.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

public class TestFunctionHelper {
    public final int second = 20;
    public final BlockPos SPONGE = new BlockPos(5, 1, 5);
    public final BlockPos SPONGE_PISTON = new BlockPos(5, 1, 6);
    public final BlockPos HEAT_SOURCE = new BlockPos(5, 2, 5);
    public final BlockPos HEAT_SOURCE_PISTON = new BlockPos(5, 2, 6);
    public final BlockPos WALL_HOLE = new BlockPos(6, 3, 5);
    public final BlockPos PISTON = new BlockPos(5, 1, 4);
    public final BlockPos REDSTONE = new BlockPos(5, 2, 4);

    public List<BlockPos> WATER_SAMPLES;
    public List<BlockPos> LOW_RANGE_SAMPLES;
    public List<BlockPos> MEDIUM_RANGE_SAMPLES;
    public List<BlockPos> HIGH_RANGE_SAMPLES;
    public List<BlockPos> HIGH_RANGE_SAMPLES_PISTON;
    public List<BlockPos> MEDIUM_RANGE_SAMPLES_PISTON;

    public final List<BlockPos> WATER_CORNERS = List.of(new BlockPos(1, 1, 1), new BlockPos(9, 1, 9));
    public final List<BlockPos> GROUND_CORNERS = List.of(new BlockPos(0, 0, 0), new BlockPos(10, 0, 10));
    public final List<BlockPos> OUTER_BORDER_CORNERS = List.of(new BlockPos(0, 1, 0), new BlockPos(10, 1, 10));
    public final List<BlockPos> INNER_BORDER_CORNERS = List.of(new BlockPos(2, 1, 2), new BlockPos(8, 1, 8));
    public final List<BlockPos> WALL_CORNERS = List.of(new BlockPos(6, 0, 0), new BlockPos(6, 10, 10));

    public List<BlockPos> WATER_SPONGE_SIDE_CORNERS = List.of(
            new BlockPos(5, 1, 1), new BlockPos(1, 1, 1),
            new BlockPos(1, 1, 9), new BlockPos(5, 1, 9));

    public List<BlockPos> WATER_OTHER_SIDE_CORNERS = List.of(
            new BlockPos(7, 1, 1), new BlockPos(9, 1, 1),
            new BlockPos(9, 1, 9), new BlockPos(7, 1, 9));

    public TestFunctionHelper(ServerLevel level) {
        var server = level.getServer();

        // Use custom ranges, as tests were made before defaults changed
        level.getGameRules().set(ModGameRules.ABSORPTION_RANGE_HIGH, 3, server);
        level.getGameRules().set(ModGameRules.ABSORPTION_RANGE_MEDIUM, 2, server);
        level.getGameRules().set(ModGameRules.ABSORPTION_RANGE_LOW, 1, server);

        var rangeHigh = ModGameRules.getAbsorptionRangeHigh(level);
        var rangeMedium = ModGameRules.getAbsorptionRangeMedium(level);
        var rangeLow = ModGameRules.getAbsorptionRangeLow(level);

        WATER_SAMPLES = getRadialSamples(SPONGE, rangeHigh + 1, false);
        LOW_RANGE_SAMPLES = getRadialSamples(SPONGE, rangeLow, false);
        MEDIUM_RANGE_SAMPLES = getRadialSamples(SPONGE, rangeMedium, false);
        HIGH_RANGE_SAMPLES = getRadialSamples(SPONGE, rangeHigh, false);
        HIGH_RANGE_SAMPLES_PISTON = getRadialSamples(SPONGE_PISTON, rangeHigh, false);
        MEDIUM_RANGE_SAMPLES_PISTON = getRadialSamples(SPONGE_PISTON, rangeMedium, false);
        MEDIUM_RANGE_SAMPLES_PISTON.remove(PISTON);

    }

    /**
     * Gets the sample positions based on the radius
     * 
     * @param center
     * @param radius
     * @param fillToRadius If true, adds samples that are inside the radius. If
     *                     false just adds the blocks on the radius
     * @return
     */
    public List<BlockPos> getRadialSamples(BlockPos center, float radius, boolean fillToRadius) {
        var result = new ArrayList<BlockPos>();

        for (int i = (int) radius; i >= 1; i--) {
            result.add(new BlockPos(center.getX() - i, center.getY(), center.getZ()));
            result.add(new BlockPos(center.getX() + i, center.getY(), center.getZ()));
            result.add(new BlockPos(center.getX(), center.getY(), center.getZ() - i));
            result.add(new BlockPos(center.getX(), center.getY(), center.getZ() + i));

            if (fillToRadius == false) {
                break;
            }
        }

        return result;
    }

    public void setBlocks(GameTestHelper context, List<BlockPos> positions, Block block) {
        for (int i = 0; i < positions.size(); i++) {
            context.setBlock(positions.get(i), block);
        }
    }

    public void filPlanes(GameTestHelper context, List<BlockPos> corners, Block block) {
        for (int i = 0; i < corners.size() - 1; i++) {
            filPlane(context, corners.get(i), corners.get(i + 1), block);
        }
    }

    public void filPlane(GameTestHelper context, BlockPos cornerA, BlockPos cornerB, Block block) {
        int xDelta = Integer.signum(cornerB.getX() - cornerA.getX());
        int yDelta = Integer.signum(cornerB.getY() - cornerA.getY());
        int zDelta = Integer.signum(cornerB.getZ() - cornerA.getZ());

        int xSteps = Math.abs(cornerB.getX() - cornerA.getX()) + 1;
        int ySteps = Math.abs(cornerB.getY() - cornerA.getY()) + 1;
        int zSteps = Math.abs(cornerB.getZ() - cornerA.getZ()) + 1;

        for (int x = 0; x < xSteps; x++) {
            for (int y = 0; y < ySteps; y++) {
                for (int z = 0; z < zSteps; z++) {
                    var pos = new BlockPos(
                            cornerA.getX() + x * xDelta,
                            cornerA.getY() + y * yDelta,
                            cornerA.getZ() + z * zDelta);

                    context.setBlock(pos, block);
                }
            }
        }
    }

    public void assertSamples(GameTestHelper context, List<BlockPos> samples, Block expected) {
        for (var blockPos : samples) {
            context.assertBlockPresent(expected, blockPos);
        }
    }

    public int getAbsorptionTime(ServerLevel level) {
        var maxTime = ModGameRules.getAbsorptionTimeMax(level);
        return second * maxTime;
    }
}
