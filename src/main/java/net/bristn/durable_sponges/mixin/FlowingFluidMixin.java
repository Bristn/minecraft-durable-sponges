package net.bristn.durable_sponges.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.bristn.durable_sponges.CommonModInitializer;
import net.bristn.durable_sponges.util.FluidUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

@Mixin(FlowingFluid.class)
abstract class FlowingFluidMixin extends Fluid {

    // TODO: Use canHoldSpecificFluid ? Would prevent lava & torch issues??

    // TODO: canHoldSpecificFluid does not refill after sponge is removed! ()
    // canHoldSpecificFluidTest wird nur einmalig aufgerufen und nicht nochmals nach
    // dem der Sponge entfernt wurde

    // ! Manuell die reachable positions aktualisieren?

    @Inject(method = "canHoldSpecificFluid", at = @At("TAIL"), cancellable = true)
    private static void canHoldSpecificFluidTest(final BlockGetter level, final BlockPos pos, final BlockState state,
            final Fluid newFluid, CallbackInfoReturnable<Boolean> callback) {

        var isWater = newFluid.isSame(Fluids.FLOWING_WATER) || newFluid.isSame(Fluids.WATER);
        if (isWater == false) {
            return;
        }

        var serverLevel = (ServerLevel) level;
        var hasAbsorbed = FluidUtility.tryAbsorbWater(serverLevel, pos, false);
        if (hasAbsorbed == true) {
            callback.setReturnValue(false);
        }

        callback.setReturnValue(callback.getReturnValue());
        // CommonModInitializer.LOGGER.error("canHoldSpecificFluid ");
    }

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
    @Inject(method = "spreadTo", at = @At("TAIL"), cancellable = true)
    protected void durableSpongeSpreadTo(final LevelAccessor level, final BlockPos waterPos, final BlockState state,
            final Direction direction, final FluidState target, CallbackInfo callback) {

        // if (level.isClientSide()) {
        // return;
        // }

        // // TODO: Cancel does not work as intended. Torches are still flushed
        // var serverLevel = (ServerLevel) level;
        // var hasAbsorbed = FluidUtility.tryAbsorbWater(serverLevel, waterPos, true);
        // if (hasAbsorbed == true) {
        // callback.cancel();
        // }

        // CommonModInitializer.LOGGER.error("Flowing fluid was absorbed: " +
        // hasAbsorbed);
    }

}
