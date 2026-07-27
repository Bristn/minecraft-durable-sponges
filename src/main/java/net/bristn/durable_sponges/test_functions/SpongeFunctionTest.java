package net.bristn.durable_sponges.test_functions;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.bristn.durable_sponges.test_utilities.TestFunctionOrder;
import net.bristn.durable_sponges.test_utilities.TestFunctionHelper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class SpongeFunctionTest {

    public SpongeFunctionTest() {
        TestFunctionOrder.resetIfFinished();
    }

    @GameTest(maxTicks = 10000)
    public void notReplacedWithoutWater(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.SPONGE_NOT_REPLACED_WITHOUT_WATER;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.AIR);
            context.setBlock(helper.SPONGE, Blocks.SPONGE);

            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.AIR);

            context.runAfterDelay(helper.second, () -> {
                context.assertBlockPresent(Blocks.SPONGE, helper.SPONGE);
                context.succeed();
                rule.succeed();
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void preventsWaterSpreading(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.SPONGE_PREVENTS_WATER_SPREADING;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var maxTime = helper.getAbsorptionTime(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);
            helper.filPlanes(context, helper.INNER_BORDER_CORNERS, Blocks.AIR);
            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);

            context.setBlock(helper.SPONGE, Blocks.SPONGE);

            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);

            context.runAfterDelay(maxTime - helper.second * 1, () -> {
                context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);

                context.runAfterDelay(helper.second * 3, () -> {
                    helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                    helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);

                    context.succeed();
                    rule.succeed();
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void replacesExistingWater(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.SPONGE_REPLACES_EXISTING_WATER;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var maxTime = helper.getAbsorptionTime(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);
            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.SPONGE);

                context.runAfterDelay(helper.second, () -> {
                    helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                    helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);

                    context.runAfterDelay(maxTime - helper.second * 2, () -> {
                        context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                        helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                        context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);

                        context.runAfterDelay(helper.second * 3, () -> {
                            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                            context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);

                            context.succeed();
                            rule.succeed();
                        });
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void doesNotReplaceWaterBehindWall(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.SPONGE_DOES_NOT_REPLACE_WATER_BEHIND_WALL;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var maxTime = helper.getAbsorptionTime(context.getLevel());
            var inInfluence = new BlockPos(helper.SPONGE.getX() - 1, helper.SPONGE.getY(), helper.SPONGE.getZ());
            var notInInfluence = new BlockPos(helper.SPONGE.getX() + 2, helper.SPONGE.getY(), helper.SPONGE.getZ());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.AIR);
            helper.filPlanes(context, helper.WALL_CORNERS, Blocks.GLASS);
            helper.filPlanes(context, helper.WATER_OTHER_SIDE_CORNERS, Blocks.WATER);
            helper.filPlanes(context, helper.WATER_SPONGE_SIDE_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.SPONGE);

                context.runAfterDelay(maxTime - helper.second * 1, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    context.assertBlockPresent(Blocks.WATER, notInInfluence);
                    context.assertBlockPresent(Blocks.AIR, inInfluence);

                    context.runAfterDelay(helper.second * 3, () -> {
                        context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                        context.assertBlockPresent(Blocks.WATER, notInInfluence);
                        context.assertBlockPresent(Blocks.WATER, inInfluence);

                        context.succeed();
                        rule.succeed();
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void absorbsAfterWallBreak(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.SPONGE_ABSORBS_AFTER_WALL_BREAK;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var inInfluence = new BlockPos(helper.SPONGE.getX() - 1, helper.SPONGE.getY(), helper.SPONGE.getZ());
            var notInInfluence = new BlockPos(helper.SPONGE.getX() + 2, helper.SPONGE.getY(), helper.SPONGE.getZ());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.AIR);
            helper.filPlanes(context, helper.WALL_CORNERS, Blocks.GLASS);
            helper.filPlanes(context, helper.WATER_OTHER_SIDE_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.SPONGE);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.SPONGE, helper.SPONGE);
                    context.assertBlockPresent(Blocks.WATER, notInInfluence);
                    context.assertBlockPresent(Blocks.AIR, inInfluence);

                    context.setBlock(helper.WALL_HOLE, Blocks.AIR);

                    context.runAfterDelay(helper.second, () -> {
                        context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                        context.assertBlockPresent(Blocks.AIR, notInInfluence);
                        context.assertBlockPresent(Blocks.AIR, inInfluence);

                        context.succeed();
                        rule.succeed();
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void absorbsAfterWallBreakWithHeat(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.SPONGE_ABSORBS_AFTER_WALL_BREAK_WITH_HEAT;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var inInfluence = new BlockPos(helper.SPONGE.getX() - 1, helper.SPONGE.getY(), helper.SPONGE.getZ());
            var notInInfluence = new BlockPos(helper.SPONGE.getX() + 2, helper.SPONGE.getY(), helper.SPONGE.getZ());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.AIR);
            helper.filPlanes(context, helper.WALL_CORNERS, Blocks.GLASS);
            helper.filPlanes(context, helper.WATER_OTHER_SIDE_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.SPONGE);
                context.setBlock(helper.HEAT_SOURCE, Blocks.LAVA_CAULDRON);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.SPONGE, helper.SPONGE);
                    context.assertBlockPresent(Blocks.WATER, notInInfluence);
                    context.assertBlockPresent(Blocks.AIR, inInfluence);

                    context.setBlock(helper.WALL_HOLE, Blocks.AIR);

                    context.runAfterDelay(helper.second, () -> {
                        context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                        context.assertBlockPresent(Blocks.AIR, notInInfluence);
                        context.assertBlockPresent(Blocks.AIR, inInfluence);

                        context.succeed();
                        rule.succeed();
                    });
                });
            });
        });
    }

}