package net.bristn.durable_sponges.poi_types;

import net.bristn.durable_sponges.CommonModInitializer;
import net.bristn.durable_sponges.block_states.ModBlockStates;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ModPoiTypes {
    public static final PoiType SPONGE_POI = register("sponge", Blocks.SPONGE);
    public static final PoiType WET_SPONGE_POI = register("wet_sponge", Blocks.WET_SPONGE);

    public static void registerModPoiTypes() {
        CommonModInitializer.LOGGER.info("Initialized ModPoiTypes for " + CommonModInitializer.MOD_ID);
    }

    public static boolean isSpongePoiType(Holder<PoiType> holder) {
        var value = holder.value();
        return value.equals(ModPoiTypes.SPONGE_POI) || value.equals(ModPoiTypes.WET_SPONGE_POI);
    }

    private static PoiType register(String name, Block... blocks) {
        var identifier = Identifier.fromNamespaceAndPath(CommonModInitializer.MOD_ID, name);
        return PoiHelper.register(identifier, 0, ModBlockStates.RANGE_MAX + 1, blocks);
    }
}
