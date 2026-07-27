package net.bristn.durable_sponges.records;

import java.util.Set;

import net.minecraft.core.BlockPos;

public record SpongeInfluence(Set<BlockPos> all, Set<BlockPos> edge, Set<BlockPos> water) {
}
