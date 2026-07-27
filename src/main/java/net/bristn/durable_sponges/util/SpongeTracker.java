package net.bristn.durable_sponges.util;

import java.util.ArrayDeque;
import java.util.HashMap;

import net.bristn.durable_sponges.CommonModInitializer;
import net.bristn.durable_sponges.records.ServerLevelAndPos;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public class SpongeTracker {
    private static long lastUpdate = 0L;
    private static HashMap<ServerLevel, HashMap<BlockPos, WetSpongeInterface>> sponges = new HashMap<>();

    /**
     * As the wet sponges are no BlockEntity, the loadAdditional method is not
     * available. To initialize the sponges whenever a chunk is loaded, the wet
     * sponges are added to the collection
     * 
     * As the onPlace injection is not called when loading from disk
     */
    public static void registerChunkLoadHandler() {
        var uninitialized = new ArrayDeque<ServerLevelAndPos>();

        // Populate the sponges collection when the chunk is loaded
        // ! This cannot call updateHeatLevel, as the level is not fully initialized
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            level.dimension();

            chunk.findBlocks(state -> state.is(Blocks.WET_SPONGE), (foundPos, foundState) -> {

                // ! Prevent by reference issues be copying the block position
                var spongePos = new BlockPos(foundPos.getX(), foundPos.getY(), foundPos.getZ());
                if (SpongeTracker.hasSponge(level, spongePos) == false) {
                    SpongeTracker.addSponge(level, spongePos, new WetSpongeInterface());
                    uninitialized.add(new ServerLevelAndPos(level, spongePos));
                }
            });
        });

        // On regular ticks, check if there are still uninitialized sponges. If so,
        // update their heat level once and flag them as being initialized
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            while (!uninitialized.isEmpty()) {
                var levelAndPos = uninitialized.removeFirst();
                var level = levelAndPos.level();
                var spongePos = levelAndPos.pos();

                if (SpongeTracker.hasSponge(level, spongePos) == true) {
                    var sponge = SpongeTracker.getWetSponge(level, spongePos);
                    sponge.updateHeatLevel(spongePos, level, true);
                } else {
                    uninitialized.addLast(new ServerLevelAndPos(level, spongePos));
                }
            }
        });

        ServerLevelEvents.UNLOAD.register((server, level) -> {
            sponges.clear();
        });
    }

    public static void registerSpongeUpdateHandler() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            var anyLevel = server.getAllLevels().iterator().next();
            var now = anyLevel.getGameTime();
            var difference = now - lastUpdate;
            if (difference < 10) {
                return;
            }

            lastUpdate = now;

            sponges.forEach((level, spongesInLevel) -> {
                spongesInLevel.forEach((spongePos, access) -> {
                    access.updateSpongeUsingWaterPosition(spongePos, level);
                });
            });
        });
    }

    /**
     * Keep track of the sponge access. If there is no collection for this level, a
     * new one is added. Otherwise the sponge is appended to the existing collection
     * 
     * @param level
     * @param position
     * @param sponge
     */
    public static void addSponge(ServerLevel level, BlockPos position, WetSpongeInterface sponge) {
        if (hasSponge(level, position)) {
            return;
        }

        CommonModInitializer.LOGGER.info("SpongeTracker: Register wet sponge " + sponge.getId() + " at " + position);
        var spongesInLevel = sponges.get(level);
        if (spongesInLevel == null) {
            spongesInLevel = new HashMap<BlockPos, WetSpongeInterface>();
        }

        spongesInLevel.put(position, sponge);
        sponges.put(level, spongesInLevel);
    }

    /**
     * Removes the sponge from being tracker. If the level does not contain any
     * sponges anymore, remove the entire level from the collection
     * 
     * @param level
     * @param position
     */
    public static void removeSponge(ServerLevel level, BlockPos position) {
        CommonModInitializer.LOGGER.info("SpongeTracker: Remove wet sponge at " + position);
        var hasSPonge = hasSponge(level, position);
        if (hasSPonge == false) {
            return;
        }

        var sponge = getWetSponge(level, position);
        var spongesInLevel = sponges.get(level);
        if (spongesInLevel == null) {
            return;
        }

        spongesInLevel.remove(position);
        if (spongesInLevel.isEmpty()) {
            sponges.remove(level);
        }

        // When removing the wet sponge, manually tick all affected water sources to
        // make them spread again
        sponge.tickAndClearWaterPositions(position, level);
    }

    /**
     * Gets the sponge access at the position within the level
     * 
     * @param level
     * @param position
     * @return
     */
    public static WetSpongeInterface getWetSponge(ServerLevel level, BlockPos position) {
        var spongesInLevel = sponges.get(level);
        if (spongesInLevel == null) {
            return null;
        }

        return spongesInLevel.get(position);
    }

    /**
     * Check if there is a sponge at the position within the given level
     * 
     * @param level
     * @param position
     * @return
     */
    public static boolean hasSponge(ServerLevel level, BlockPos position) {
        var hasLevel = sponges.containsKey(level);
        if (hasLevel == false) {
            return false;
        }

        var spongesInLevel = sponges.get(level);
        return spongesInLevel.containsKey(position);
    }
}
