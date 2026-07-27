package net.bristn.durable_sponges.records;

import java.util.HashSet;

import net.minecraft.core.BlockPos;

public record AbsorptionResult(HashSet<BlockPos> waterPos, BlockPos spongePos, boolean hasAbsorbed) {

}
