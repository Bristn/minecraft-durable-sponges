package net.bristn.durable_sponges.test_functions;

import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.bristn.durable_sponges.test_utilities.TestFunctionOrder;
import net.bristn.durable_sponges.util.SpongeTracker;
import net.bristn.durable_sponges.test_utilities.TestFunctionHelper;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class WetSpongeFunctionTest implements CustomTestMethodInvoker {
    public WetSpongeFunctionTest() {
        TestFunctionOrder.resetIfFinished();
    }

    @GameTest(maxTicks = 10000)
    public void doesNothingWithoutWater(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_DOES_NOTHING_WITHOUT_WATER;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.AIR);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);

                helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.AIR);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    context.succeed();
                    rule.succeed();
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void doesNothingWithoutHeat(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_DOES_NOTHING_WITHOUT_HEAT;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);

                helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    context.succeed();
                    rule.succeed();
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void absorbsHighRange(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_ABSORBS_HIGH_RANGE;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var maxTime = helper.getAbsorptionTime(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);

                helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    context.setBlock(helper.HEAT_SOURCE, Blocks.LAVA_CAULDRON);

                    context.runAfterDelay(helper.second, () -> {
                        context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                        helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);

                        context.runAfterDelay(maxTime + helper.second * 2, () -> {
                            context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);

                            context.destroyBlock(helper.SPONGE);

                            context.runAfterDelay(maxTime + helper.second * 2, () -> {
                                context.assertBlockPresent(Blocks.WATER, helper.SPONGE);
                                helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                                helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                                helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.WATER);
                                helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.WATER);

                                context.succeed();
                                rule.succeed();
                            });
                        });
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void absorbsMediumRange(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_ABSORBS_MEDIUM_RANGE;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var maxTime = helper.getAbsorptionTime(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);

                helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.WATER);
                helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    context.setBlock(helper.HEAT_SOURCE, Blocks.MAGMA_BLOCK);

                    context.runAfterDelay(helper.second, () -> {
                        context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                        helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);

                        context.runAfterDelay(maxTime + helper.second * 2, () -> {
                            context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                            helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);

                            context.destroyBlock(helper.SPONGE);

                            context.runAfterDelay(maxTime + helper.second * 2, () -> {
                                context.assertBlockPresent(Blocks.WATER, helper.SPONGE);
                                helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                                helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                                helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.WATER);
                                helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.WATER);

                                context.succeed();
                                rule.succeed();
                            });
                        });
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void absorbsLowRange(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_ABSORBS_LOW_RANGE;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var maxTime = helper.getAbsorptionTime(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);

                helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.WATER);
                helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    context.setBlock(helper.HEAT_SOURCE, Blocks.TORCH);

                    context.runAfterDelay(helper.second, () -> {
                        context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                        helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.WATER);
                        helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);

                        context.runAfterDelay(maxTime + helper.second * 2, () -> {
                            context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                            helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.WATER);
                            helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);

                            context.destroyBlock(helper.SPONGE);

                            context.runAfterDelay(maxTime + helper.second * 2, () -> {
                                context.assertBlockPresent(Blocks.WATER, helper.SPONGE);
                                helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                                helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                                helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.WATER);
                                helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.WATER);

                                context.succeed();
                                rule.succeed();
                            });
                        });
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void changesHeatSourceAtRuntime(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_CHANGES_HEAT_SOURCE_AT_RUNTIME;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var maxTime = helper.getAbsorptionTime(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    context.setBlock(helper.HEAT_SOURCE, Blocks.TORCH);

                    context.runAfterDelay(helper.second, () -> {
                        helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                        helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.WATER);
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);

                        context.setBlock(helper.HEAT_SOURCE, Blocks.MAGMA_BLOCK);

                        context.runAfterDelay(helper.second, () -> {
                            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                            helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);

                            context.setBlock(helper.HEAT_SOURCE, Blocks.LAVA_CAULDRON);

                            context.runAfterDelay(helper.second, () -> {
                                helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                                helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                                helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                                helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);

                                context.setBlock(helper.HEAT_SOURCE, Blocks.AIR);

                                context.runAfterDelay(helper.second, () -> {
                                    helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                                    helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                                    helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                                    helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);

                                    context.runAfterDelay(maxTime + helper.second * 3, () -> {
                                        helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);
                                        helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.WATER);
                                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES,
                                                Blocks.WATER);
                                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);

                                        context.succeed();
                                        rule.succeed();
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void doesDryWithMaxHeat(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_DOES_DRY_WITH_MAX_HEAT;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var level = context.getLevel();
            var helper = new TestFunctionHelper(level);

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.AIR);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    context.assertTrue(
                            SpongeTracker.hasSponge(level, context.absolutePos(helper.SPONGE)),
                            Component.literal("Tracker should contain sponge"));

                    context.setBlock(helper.HEAT_SOURCE, Blocks.LAVA_CAULDRON);

                    context.runAfterDelay(helper.second, () -> {
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                        context.randomTick(helper.SPONGE);

                        context.runAfterDelay(helper.second, () -> {
                            context.assertBlockPresent(Blocks.SPONGE, helper.SPONGE);
                            context.assertFalse(
                                    SpongeTracker.hasSponge(level, context.absolutePos(helper.SPONGE)),
                                    Component.literal("Tracker should not contain sponge"));

                            context.succeed();
                            rule.succeed();
                        });
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void doesNotDryWithWater(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_DOES_NOT_DRY_WITH_WATER;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    context.setBlock(helper.HEAT_SOURCE, Blocks.LAVA_CAULDRON);

                    context.runAfterDelay(helper.second, () -> {
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                        context.randomTick(helper.SPONGE);

                        context.runAfterDelay(helper.second, () -> {
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
    public void movedByPistonWithoutHeat(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_MOVED_BY_PISTON_WITHOUT_HEAT;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var newSponge = new BlockPos(helper.SPONGE.getX(), helper.SPONGE.getY(), helper.SPONGE.getZ() + 1);

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);
                context.setBlock(helper.PISTON, Blocks.STICKY_PISTON, Direction.SOUTH);

                helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.WATER);
                helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                context.setBlock(helper.REDSTONE, Blocks.REDSTONE_BLOCK);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, newSponge);

                    helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.WATER);
                    helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);
                    context.setBlock(helper.REDSTONE, Blocks.AIR);

                    context.runAfterDelay(helper.second, () -> {
                        context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);

                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.WATER);
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.WATER);

                        context.succeed();
                        rule.succeed();
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void movedByPistonWithHeat(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_MOVED_BY_PISTON_WITH_HEAT;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var maxTime = helper.getAbsorptionTime(context.getLevel());
            var newSponge = new BlockPos(helper.SPONGE.getX(), helper.SPONGE.getY(), helper.SPONGE.getZ() + 1);

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.HEAT_SOURCE, Blocks.LAVA_CAULDRON);
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);
                context.setBlock(helper.PISTON, Blocks.STICKY_PISTON, Direction.SOUTH);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                    helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                    // Move the sponge block away from the heat source
                    context.setBlock(helper.REDSTONE, Blocks.REDSTONE_BLOCK);
                    context.runAfterDelay(maxTime + helper.second * 2, () -> {
                        context.assertBlockPresent(Blocks.WET_SPONGE, newSponge);
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES_PISTON, Blocks.WATER);
                        helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                        // Move sponge back to heat source
                        context.setBlock(helper.REDSTONE, Blocks.AIR);
                        context.runAfterDelay(helper.second, () -> {
                            context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                            context.succeed();
                            rule.succeed();
                        });
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void movedByPistonHeatChange(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_MOVED_BY_PISTON_CHANGE_HEAT;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());
            var maxTime = helper.getAbsorptionTime(context.getLevel());
            var newSponge = new BlockPos(helper.SPONGE.getX(), helper.SPONGE.getY(), helper.SPONGE.getZ() + 1);

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.HEAT_SOURCE, Blocks.LAVA_CAULDRON);
                context.setBlock(helper.HEAT_SOURCE_PISTON, Blocks.MAGMA_BLOCK);
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);
                context.setBlock(helper.PISTON, Blocks.STICKY_PISTON, Direction.SOUTH);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                    helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                    // Move the sponge block away from the heat source
                    context.setBlock(helper.REDSTONE, Blocks.REDSTONE_BLOCK);
                    context.runAfterDelay(maxTime + helper.second * 3, () -> {
                        context.assertBlockPresent(Blocks.WET_SPONGE, newSponge);
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES_PISTON, Blocks.WATER);
                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES_PISTON, Blocks.AIR);
                        helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                        // Move sponge back to heat source
                        context.setBlock(helper.REDSTONE, Blocks.AIR);
                        context.runAfterDelay(helper.second, () -> {
                            context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                            context.succeed();
                            rule.succeed();
                        });
                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void doesNotBreakTorchOnEdge(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_DOES_NOT_BREAK_TORCH_ON_EDGE;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);
                context.setBlock(helper.HEAT_SOURCE, Blocks.LAVA_CAULDRON);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                    helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                    helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                    helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                    context.runAfterDelay(helper.second, () -> {
                        helper.setBlocks(context, helper.HIGH_RANGE_SAMPLES, Blocks.TORCH);
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.TORCH);
                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                        context.runAfterDelay(helper.second, () -> {
                            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.TORCH);
                            helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                            context.succeed();
                            rule.succeed();
                        });

                    });
                });
            });
        });
    }

    @GameTest(maxTicks = 10000)
    public void doesNotCreateObsidianOnEdge(GameTestHelper context) {
        var group = TestFunctionOrder.SPONGE_GROUP;
        var rule = TestFunctionOrder.WET_SPONGE_DOES_NOT_CREATE_OBSIDIAN_ON_EDGE;

        TestFunctionOrder.waitForTestGroup(context, group, () -> {
            var helper = new TestFunctionHelper(context.getLevel());

            helper.filPlanes(context, helper.GROUND_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.OUTER_BORDER_CORNERS, Blocks.STONE);
            helper.filPlanes(context, helper.WATER_CORNERS, Blocks.WATER);

            context.runAfterDelay(helper.second, () -> {
                context.setBlock(helper.SPONGE, Blocks.WET_SPONGE);
                context.setBlock(helper.HEAT_SOURCE, Blocks.LAVA_CAULDRON);

                context.runAfterDelay(helper.second, () -> {
                    context.assertBlockPresent(Blocks.WET_SPONGE, helper.SPONGE);
                    helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES, Blocks.AIR);
                    helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                    helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                    helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                    context.runAfterDelay(helper.second, () -> {
                        helper.setBlocks(context, helper.HIGH_RANGE_SAMPLES_LAVA, Blocks.LAVA);
                        helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES_LAVA, Blocks.LAVA);
                        helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                        helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                        context.runAfterDelay(helper.second, () -> {
                            helper.assertSamples(context, helper.HIGH_RANGE_SAMPLES_LAVA, Blocks.LAVA);
                            helper.assertSamples(context, helper.MEDIUM_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.LOW_RANGE_SAMPLES, Blocks.AIR);
                            helper.assertSamples(context, helper.WATER_SAMPLES, Blocks.WATER);

                            context.succeed();
                            rule.succeed();
                        });

                    });
                });
            });
        });
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        method.invoke(this, context);
    }
}