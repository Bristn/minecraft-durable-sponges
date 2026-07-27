package net.bristn.durable_sponges.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.bristn.durable_sponges.util.AbsorptionUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(BucketItem.class)
public class BucketItemMixin {

    /**
     * Before placing a water bucket, check if there are nearby sponges with
     * remaining absorption time. If there are any, prevent the water from being
     * placed
     * 
     * @param user
     * @param level
     * @param waterPos
     * @param hitResult
     * @param callback
     */
    @Inject(method = "emptyContents", at = @At("HEAD"), cancellable = true)
    private void durableSpongeEmptyContents(final LivingEntity user, final Level level, final BlockPos waterPos,
            final BlockHitResult hitResult, CallbackInfoReturnable<Boolean> callback) {

        if (level.isClientSide()) {
            return;
        }

        var bucket = (BucketItem) (Object) this;
        var fluid = bucket.getContent();
        if (fluid.isSame(Fluids.WATER) == false) {
            return;
        }

        var serverLevel = (ServerLevel) level;
        var result = AbsorptionUtility.tryAbsorbWater(serverLevel, waterPos, false);

        // If the water would have been absorbed by a sponge, prevent placing the water
        if (result.hasAbsorbed() == true) {
            callback.setReturnValue(false);
            callback.cancel();
            return;
        }
    }
}
