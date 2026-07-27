package net.bristn.durable_sponges.records;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record ServerLevelAndPos(ServerLevel level, BlockPos pos) {
}
