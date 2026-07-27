package net.bristn.durable_sponges.tags.generator;

import java.util.concurrent.CompletableFuture;

import net.bristn.durable_sponges.tags.ModBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.references.BlockIds;
import net.minecraft.references.BlockItemIds;

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
        builder(ModBlockTags.LOW_HEAT)
                .add(BlockItemIds.TORCH.block(), BlockIds.WALL_TORCH)
                .add(BlockItemIds.COPPER_TORCH.block(), BlockIds.COPPER_WALL_TORCH)
                .add(BlockItemIds.SOUL_TORCH.block(), BlockIds.SOUL_WALL_TORCH)
                .add(BlockItemIds.REDSTONE_TORCH.block(), BlockIds.REDSTONE_WALL_TORCH)
                .add(BlockItemIds.GLOW_LICHEN)
                .add(BlockItemIds.LANTERN, BlockItemIds.SOUL_LANTERN)
                .add(BlockItemIds.REDSTONE_ORE, BlockItemIds.DEEPSLATE_REDSTONE_ORE)
                .setReplace(false);

        builder(ModBlockTags.MEDIUM_HEAT)
                .add(BlockItemIds.MAGMA_BLOCK)
                .add(BlockItemIds.VERDANT_FROGLIGHT, BlockItemIds.OCHRE_FROGLIGHT, BlockItemIds.PEARLESCENT_FROGLIGHT)
                .add(BlockItemIds.GLOWSTONE, BlockItemIds.JACK_O_LANTERN, BlockItemIds.SEA_LANTERN,
                        BlockItemIds.SHROOMLIGHT)
                .setReplace(false);

        builder(ModBlockTags.HIGH_HEAT)
                .add(BlockIds.LAVA, BlockIds.LAVA_CAULDRON)
                .add(BlockIds.FIRE, BlockItemIds.CAMPFIRE.block())
                .add(BlockIds.SOUL_FIRE, BlockItemIds.SOUL_CAMPFIRE.block())
                .setReplace(false);
    }
}
