package net.bristn.durable_sponges.game_rules;

import net.bristn.durable_sponges.CommonModInitializer;
import net.bristn.durable_sponges.block_states.ModBlockStates;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class ModGameRules {
    public static final GameRule<Integer> DELAY_BEFORE_DRYING = registerInt(2, "delay_before_drying");
    public static final GameRule<Integer> ABSORPTION_TIME_MAX = registerInt(3, "absorption_time_max");
    public static final GameRule<Integer> ABSORPTION_RANGE_LOW = registerInt(2, "absorption_range_low");
    public static final GameRule<Integer> ABSORPTION_RANGE_MEDIUM = registerInt(3, "absorption_range_medium");
    public static final GameRule<Integer> ABSORPTION_RANGE_HIGH = registerInt(4, "absorption_range_high");

    public static void registerModGameRules() {
        CommonModInitializer.LOGGER.info("Register ModGameRules for" + CommonModInitializer.MOD_ID);
    }

    public static int getDelayBeforeDrying(ServerLevel level) {
        var value = level.getGameRules().get(ModGameRules.DELAY_BEFORE_DRYING);
        return Math.max(value, 0);
    }

    public static int getAbsorptionTimeMax(ServerLevel level) {
        var value = level.getGameRules().get(ModGameRules.ABSORPTION_TIME_MAX);
        return Math.clamp(value, 1, ModBlockStates.TIME_MAX);
    }

    /***
     * Utility to get the low radius of absorption from the game rule. Adds an
     * additional 0.75 to make the radius more rounded without extending the main
     * axis to the next block
     * 
     * @param level
     * @return
     */
    public static float getAbsorptionRangeLow(ServerLevel level) {
        var value = level.getGameRules().get(ModGameRules.ABSORPTION_RANGE_LOW);
        var clamped = Math.clamp(value, 1, ModBlockStates.RANGE_MAX);
        return clamped + 0.75f;
    }

    /***
     * Utility to get the medium radius of absorption from the game rule. Adds an
     * additional 0.75 to make the radius more rounded without extending the main
     * axis to the next block
     * 
     * @param level
     * @return
     */
    public static float getAbsorptionRangeMedium(ServerLevel level) {
        var value = level.getGameRules().get(ModGameRules.ABSORPTION_RANGE_MEDIUM);
        var clamped = Math.clamp(value, 1, ModBlockStates.RANGE_MAX);
        return clamped + 0.75f;
    }

    /***
     * Utility to get the high radius of absorption from the game rule. Adds an
     * additional 0.75 to make the radius more rounded without extending the main
     * axis to the next block
     * 
     * @param level
     * @return
     */
    public static float getAbsorptionRangeHigh(ServerLevel level) {
        var value = level.getGameRules().get(ModGameRules.ABSORPTION_RANGE_HIGH);
        var clamped = Math.clamp(value, 1, ModBlockStates.RANGE_MAX);
        return clamped + 0.75f;
    }

    private static GameRule<Integer> registerInt(int defaultValue, String name) {
        var identifier = Identifier.fromNamespaceAndPath(CommonModInitializer.MOD_ID, name);
        return GameRuleBuilder.forInteger(defaultValue).category(GameRuleCategory.UPDATES).buildAndRegister(identifier);
    }
}
