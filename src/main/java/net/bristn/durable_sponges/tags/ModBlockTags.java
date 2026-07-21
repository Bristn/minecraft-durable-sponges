package net.bristn.durable_sponges.tags;

import net.bristn.durable_sponges.CommonModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    public static final TagKey<Block> LOW_HEAT = registerTag("low_heat");
    public static final TagKey<Block> MEDIUM_HEAT = registerTag("medium_heat");
    public static final TagKey<Block> HIGH_HEAT = registerTag("high_heat");

    public static void registerModItemTags() {
        CommonModInitializer.LOGGER.info("Register ModItemTags for" + CommonModInitializer.MOD_ID);
    }

    private static TagKey<Block> registerTag(String name) {
        var identifier = Identifier.fromNamespaceAndPath(CommonModInitializer.MOD_ID, name);
        return TagKey.create(Registries.BLOCK, identifier);
    }
}
