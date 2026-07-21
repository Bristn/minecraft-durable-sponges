package net.bristn.durable_sponges.tags.generator;

import java.util.concurrent.CompletableFuture;

import net.bristn.durable_sponges.tags.ModBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.level.block.Blocks;

/**
 * Adds custom item tags to group certain items into one icon in the supported
 * item section. Vanilla has some tags like "SWORDS", "AXES", etc, but some item
 * groups don't have these tags. Therefore these are created here
 */
public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {

    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(Provider registries) {
        valueLookupBuilder(ModBlockTags.LOW_HEAT)
                .add(Blocks.TORCH, Blocks.WALL_TORCH)
                .add(Blocks.COPPER_TORCH, Blocks.COPPER_WALL_TORCH)
                .add(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH)
                .add(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH)
                .add(Blocks.GLOW_LICHEN)
                .add(Blocks.LANTERN, Blocks.SOUL_LANTERN)
                .add(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE)
                .setReplace(false);

        valueLookupBuilder(ModBlockTags.MEDIUM_HEAT)
                .add(Blocks.MAGMA_BLOCK)
                .add(Blocks.VERDANT_FROGLIGHT, Blocks.OCHRE_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT)
                .add(Blocks.GLOWSTONE, Blocks.JACK_O_LANTERN, Blocks.SEA_LANTERN, Blocks.SHROOMLIGHT)
                .setReplace(false);

        valueLookupBuilder(ModBlockTags.HIGH_HEAT)
                .add(Blocks.LAVA, Blocks.LAVA_CAULDRON)
                .add(Blocks.FIRE, Blocks.CAMPFIRE)
                .add(Blocks.SOUL_FIRE, Blocks.SOUL_CAMPFIRE)
                .setReplace(false);
    }
}
