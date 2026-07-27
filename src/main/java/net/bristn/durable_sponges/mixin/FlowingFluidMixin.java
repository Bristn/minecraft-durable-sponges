package net.bristn.durable_sponges.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.bristn.durable_sponges.util.AbsorptionUtility;
import net.bristn.durable_sponges.util.SpongeTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

@Mixin(FlowingFluid.class)
abstract class FlowingFluidMixin extends Fluid {
    /**
     * Before spreading, check if there is a nearby sponge with remaining absorption
     * time. If there is one, prevent spreading
     * 
     * @param level
     * @param waterPos
     * @param state
     * @param direction
     * @param target
     * @param callback
     */
    @Inject(method = "spreadTo", at = @At("HEAD"), cancellable = true)
    protected void durableSpongeSpreadTo(final LevelAccessor level, final BlockPos waterPos, final BlockState state,
            final Direction direction, final FluidState target, CallbackInfo callback) {

        if (level.isClientSide()) {
            return;
        }

        var severLevel = (ServerLevel) level;
        var result = AbsorptionUtility.tryAbsorbWater(severLevel, waterPos, true);
        if (result.hasAbsorbed() == false) {
            return;
        }

        callback.cancel();

        // If the spreading to the new position has been prevented, keep track of the
        // original water position. The sponge ticks this water position periodically to
        // check if the sponge can still absorb water
        var spongePos = result.spongePos();
        var access = SpongeTracker.getWetSponge(severLevel, spongePos);
        var neighbors = BlockPos.withinManhattan(waterPos, 1, 1, 1);
        for (var neighbor : neighbors) {
            var isNeighborWater = AbsorptionUtility.isWater(neighbor, (ServerLevel) level);
            if (isNeighborWater == false) {
                continue;
            }

            access.addWaterPosition(neighbor, severLevel);
        }
    }
}
