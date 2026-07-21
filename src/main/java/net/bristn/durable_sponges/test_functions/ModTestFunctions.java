package net.bristn.durable_sponges.test_functions;

import java.util.function.Consumer;

import net.bristn.durable_sponges.CommonModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;

/**
 * Having the tests in the main package allows adding a json in the
 * "test_instance" so the test may be run visually in game which allows easier
 * debugging
 */
public class ModTestFunctions {

    public static void registerModTestRunner() {
        CommonModInitializer.LOGGER.info("Initialized ModTestRunner for " + CommonModInitializer.MOD_ID);

        registerSpongeTestFunctions();
        registerWetSpongeTestFunctions();
    }

    private static void registerSpongeTestFunctions() {
        var NOT_REPLACED_WITHOUT_WATER = getId("sponge_not_replaced_without_water");
        var PREVENTS_WATER_SPREADING = getId("sponge_prevents_water_spreading");
        var REPLACES_EXISTING_WATER = getId("sponge_replaces_existing_water");
        var ABSORBS_aFTER_WALL_BREAK = getId("sponge_absorbs_after_wall_break");
        var ABSORBS_aFTER_WALL_BREAK_WITH_HEAT = getId("sponge_absorbs_after_wall_break_with_heat");
        var DOES_NOT_REPLACE_WATER_BEHIND_WALL = getId("sponge_does_not_replace_water_behind_wall");

        Registry.register(BuiltInRegistries.TEST_FUNCTION,
                NOT_REPLACED_WITHOUT_WATER,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new SpongeFunctionTest();
                    instance.notReplacedWithoutWater(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, PREVENTS_WATER_SPREADING,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new SpongeFunctionTest();
                    instance.preventsWaterSpreading(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, REPLACES_EXISTING_WATER,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new SpongeFunctionTest();
                    instance.replacesExistingWater(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, ABSORBS_aFTER_WALL_BREAK,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new SpongeFunctionTest();
                    instance.absorbsAfterWallBreak(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION,
                ABSORBS_aFTER_WALL_BREAK_WITH_HEAT,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new SpongeFunctionTest();
                    instance.absorbsAfterWallBreakWithHeat(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION,
                DOES_NOT_REPLACE_WATER_BEHIND_WALL,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new SpongeFunctionTest();
                    instance.doesNotReplaceWaterBehindWall(helper);
                });
    }

    private static void registerWetSpongeTestFunctions() {
        var DOES_NOTHING_WITHOUT_WATER = getId("wet_sponge_does_nothing_without_water");
        var DOES_NOTHING_WITHOUT_HEAT = getId("wet_sponge_does_nothing_without_heat");
        var ABSORBS_HIGH_RANGE = getId("wet_sponge_absorbs_high_range");
        var ABSORBS_MEDIUM_RANGE = getId("wet_sponge_absorbs_medium_range");
        var ABSORBS_LOW_RANGE = getId("wet_sponge_absorbs_low_range");
        var CHANGES_HEAT_SOURCE_AT_RUNTIME = getId("wet_sponge_changes_heat_source_at_runtime");
        var DOES_DRY_WITH_MAX_HEAT = getId("wet_sponge_does_dry_with_max_heat");
        var DOES_NOT_DRY_WITH_WATER = getId("wet_sponge_does_not_dry_with_water");
        var MOVED_BY_PISTON_WITHOUT_HEAT = getId("wet_sponge_moved_by_piston_without_heat");
        var MOVED_BY_PISTON_WITH_HEAT = getId("wet_sponge_moved_by_piston_with_heat");
        var MOVED_BY_PISTON_CHANGE_HEAT = getId("wet_sponge_moved_by_piston_change_heat");
        var DOES_NOT_BREAK_TORCH_ON_EDGE = getId("wet_sponge_does_not_break_torch_on_edge");

        Registry.register(BuiltInRegistries.TEST_FUNCTION,
                DOES_NOTHING_WITHOUT_WATER,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.doesNothingWithoutWater(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, DOES_NOTHING_WITHOUT_HEAT,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.doesNothingWithoutHeat(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, ABSORBS_HIGH_RANGE,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.absorbsHighRange(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, ABSORBS_MEDIUM_RANGE,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.absorbsMediumRange(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, ABSORBS_LOW_RANGE,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.absorbsLowRange(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, CHANGES_HEAT_SOURCE_AT_RUNTIME,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.changesHeatSourceAtRuntime(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, DOES_DRY_WITH_MAX_HEAT,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.doesDryWithMaxHeat(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, DOES_NOT_DRY_WITH_WATER,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.doesNotDryWithWater(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, MOVED_BY_PISTON_WITHOUT_HEAT,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.movedByPistonWithoutHeat(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, MOVED_BY_PISTON_WITH_HEAT,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.movedByPistonWithHeat(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, MOVED_BY_PISTON_CHANGE_HEAT,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.movedByPistonHeatChange(helper);
                });

        Registry.register(BuiltInRegistries.TEST_FUNCTION, DOES_NOT_BREAK_TORCH_ON_EDGE,
                (Consumer<GameTestHelper>) (helper) -> {
                    var instance = new WetSpongeFunctionTest();
                    instance.doesNotBreakTorchOnEdge(helper);
                });
    }

    private static Identifier getId(String name) {
        return Identifier.fromNamespaceAndPath(CommonModInitializer.MOD_ID, name);
    }
}
