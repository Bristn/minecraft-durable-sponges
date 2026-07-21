package net.bristn.durable_sponges.test_utilities;

import java.util.List;

import net.minecraft.gametest.framework.GameTestHelper;

public class TestFunctionOrder {

    public static TestFunctionOrder SPONGE_NOT_REPLACED_WITHOUT_WATER = new TestFunctionOrder(false);
    public static TestFunctionOrder SPONGE_PREVENTS_WATER_SPREADING = new TestFunctionOrder(false);
    public static TestFunctionOrder SPONGE_REPLACES_EXISTING_WATER = new TestFunctionOrder(false);
    public static TestFunctionOrder SPONGE_ABSORBS_AFTER_WALL_BREAK = new TestFunctionOrder(false);
    public static TestFunctionOrder SPONGE_ABSORBS_AFTER_WALL_BREAK_WITH_HEAT = new TestFunctionOrder(false);
    public static TestFunctionOrder SPONGE_DOES_NOT_REPLACE_WATER_BEHIND_WALL = new TestFunctionOrder(false);

    public static TestFunctionOrder WET_SPONGE_DOES_NOTHING_WITHOUT_WATER = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_DOES_NOTHING_WITHOUT_HEAT = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_ABSORBS_LOW_RANGE = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_ABSORBS_MEDIUM_RANGE = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_ABSORBS_HIGH_RANGE = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_CHANGES_HEAT_SOURCE_AT_RUNTIME = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_DOES_DRY_WITH_MAX_HEAT = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_DOES_NOT_DRY_WITH_WATER = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_MOVED_BY_PISTON_WITHOUT_HEAT = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_MOVED_BY_PISTON_WITH_HEAT = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_MOVED_BY_PISTON_CHANGE_HEAT = new TestFunctionOrder(false);
    public static TestFunctionOrder WET_SPONGE_DOES_NOT_BREAK_TORCH_ON_EDGE = new TestFunctionOrder(false);

    public static List<TestFunctionOrder> ORDER = List.of(
            SPONGE_NOT_REPLACED_WITHOUT_WATER,
            SPONGE_PREVENTS_WATER_SPREADING,
            SPONGE_REPLACES_EXISTING_WATER,
            SPONGE_ABSORBS_AFTER_WALL_BREAK,
            SPONGE_ABSORBS_AFTER_WALL_BREAK_WITH_HEAT,
            SPONGE_DOES_NOT_REPLACE_WATER_BEHIND_WALL,

            WET_SPONGE_DOES_NOTHING_WITHOUT_WATER,
            WET_SPONGE_DOES_NOTHING_WITHOUT_HEAT,
            WET_SPONGE_ABSORBS_LOW_RANGE,
            WET_SPONGE_ABSORBS_MEDIUM_RANGE,
            WET_SPONGE_ABSORBS_HIGH_RANGE,
            WET_SPONGE_CHANGES_HEAT_SOURCE_AT_RUNTIME,
            WET_SPONGE_DOES_DRY_WITH_MAX_HEAT,
            WET_SPONGE_DOES_NOT_DRY_WITH_WATER,
            WET_SPONGE_MOVED_BY_PISTON_WITHOUT_HEAT,
            WET_SPONGE_MOVED_BY_PISTON_WITH_HEAT,
            WET_SPONGE_MOVED_BY_PISTON_CHANGE_HEAT,
            WET_SPONGE_DOES_NOT_BREAK_TORCH_ON_EDGE);

    public boolean hasSucceeded;

    public TestFunctionOrder(boolean hasSucceeded) {
        this.hasSucceeded = hasSucceeded;
    }

    public void succeed() {
        this.hasSucceeded = true;
    }

    public static void resetIfFinished() {
        for (var test : ORDER) {
            if (test.hasSucceeded == false) {
                return;
            }
        }

        for (var test : ORDER) {
            test.hasSucceeded = false;
        }
    }

    public static void waitForTestToSucceed(GameTestHelper context, TestFunctionOrder order,
            List<TestFunctionOrder> list, Runnable callback) {

        context.runAfterDelay(5, () -> {
            var index = list.indexOf(order);
            if (index == 0) {
                callback.run();
                return;
            }

            if (list.get(index - 1).hasSucceeded == true) {
                callback.run();
                return;
            }

            waitForTestToSucceed(context, order, list, callback);
        });
    }
}
