package net.bristn.durable_sponges;

import net.bristn.durable_sponges.game_rules.ModGameRules;
import net.bristn.durable_sponges.poi_types.ModPoiTypes;
import net.bristn.durable_sponges.test_functions.ModTestFunctions;
import net.bristn.durable_sponges.util.SpongeTracker;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonModInitializer implements ModInitializer {
    public static final String MOD_ID = "durable_sponges";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Register ModInitializer for " + MOD_ID);

        ModPoiTypes.registerModPoiTypes();
        ModTestFunctions.registerModTestRunner();
        ModGameRules.registerModGameRules();

        SpongeTracker.registerChunkLoadHandler();
        SpongeTracker.registerSpongeUpdateHandler();
    }
}