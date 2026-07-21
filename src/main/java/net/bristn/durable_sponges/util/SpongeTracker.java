package net.bristn.durable_sponges.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import net.bristn.durable_sponges.CommonModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public class SpongeTracker {

    // TODO: Is not correctly initialized when loading the map.
    // ! -> nicht alle Sponges werden korrekt geladen, aber manche schon?

    private static HashMap<BlockPos, WetSpongeInterface> sponges = new HashMap<>();

    /**
     * As the wet sponges are no BlockEntity, the loadAdditional method is not
     * available. To initialize the sponges whenever a chunk is loaded, the wet
     * sponges are added to the collection
     * 
     * As the onPlace injection is not called when loading from disk
     */
    public static void registerChunkLoadHandler() {
        var uninitialized = new ArrayDeque<BlockPos>();

        // Populate the sponges collection when the chunk is loaded
        // ! This cannot call updateHeatLevel, as the level is not fully initialized
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            // TODO: Also track the dimension of the sponge (Update register methods to
            // include level/dimension)!
            level.dimension();

            chunk.findBlocks(state -> state.is(Blocks.WET_SPONGE), (foundPos, foundState) -> {
                if (SpongeTracker.hasSponge(foundPos) == false) {
                    SpongeTracker.addSponge(foundPos, new WetSpongeInterface());
                    uninitialized.add(foundPos);
                }
            });
        });

        // On regular ticks, check if there are still uninitialized sponges. If so,
        // update their heat level once and flag them as being initialized
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            while (!uninitialized.isEmpty()) {
                var blockPos = uninitialized.removeFirst();

                if (SpongeTracker.hasSponge(blockPos) == true) {
                    var sponge = SpongeTracker.getWetSponge(blockPos);
                    sponge.updateHeatLevel(blockPos, server.overworld()); // TODO: Dont harcode overworld
                } else {
                    uninitialized.addLast(blockPos);
                }
            }
        });

        ServerLevelEvents.UNLOAD.register((server, level) -> {
            sponges.clear();
        });
    }

    public static void addSponge(BlockPos position, WetSpongeInterface sponge) {
        if (sponges.containsKey(position)) {
            return;
        }

        CommonModInitializer.LOGGER.info("SpongeTracker: Register wet sponge " + sponge.getId() + " at " + position);
        sponges.put(position, sponge);
    }

    public static void removeSponge(ServerLevel level, BlockPos position) {
        CommonModInitializer.LOGGER.info("SpongeTracker: Remove wet sponge at " + position);
        sponges.remove(position);

        // TODO: Call spread of nearby water blocks?

        var positions = SpongeUtility.getWaterPositions(position, level);
        CommonModInitializer.LOGGER.info(positions.toString());

        for (var blockPos : positions) {
            var fluidState = level.getBlockState(blockPos).getFluidState();
            fluidState.tick(level, blockPos, level.getBlockState(blockPos));
        }

    }

    public static WetSpongeInterface getWetSponge(BlockPos position) {
        var sponge = sponges.get(position);
        return sponge;
    }

    public static boolean hasSponge(BlockPos position) {
        var hasSponge = sponges.containsKey(position);
        return hasSponge;
    }
}
