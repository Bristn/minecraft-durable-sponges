package net.bristn.durable_sponges.block_states;

import net.bristn.durable_sponges.CommonModInitializer;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ModBlockStates {
    /**
     * Set the upper limit of the absorption time to be 10 seconds. Has a hard
     * limit, as all possible times are encoded in the block states
     */
    public static int TIME_MAX = 10;

    /**
     * Hardcodes a max range limit used when registering the POI type
     */
    public static int RANGE_MAX = 8;

    /**
     * Helper to keep track of the max heat level
     */
    public static int HEAT_LEVEL_MAX = 3;

    public static final IntegerProperty ABSORPTION_TIME = IntegerProperty.create("absorption_time", 0, TIME_MAX);
    public static final IntegerProperty HEAT_LEVEL = IntegerProperty.create("heat_level", 0, HEAT_LEVEL_MAX);
    public static final IntegerProperty LAST_HEAT_LEVEL = IntegerProperty.create("last_heat_level", 0, HEAT_LEVEL_MAX);

    public static void registerModBlockStates() {
        CommonModInitializer.LOGGER.info("Register ModBlockStates for" + CommonModInitializer.MOD_ID);
    }
}