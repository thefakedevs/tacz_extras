package io.github.thefakedevs.taczextras.mixin.common;

import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.thefakedevs.taczextras.gameplay.BoltStateAccess;

@Mixin(value = ShooterDataHolder.class, remap = false)
public abstract class ShooterDataHolderMixin implements BoltStateAccess {
    @Unique
    private @Nullable ItemStack taczExtras$boltingGunItem;

    @Override
    public @Nullable ItemStack taczExtras$getBoltingGunItem() {
        return this.taczExtras$boltingGunItem;
    }

    @Override
    public void taczExtras$setBoltingGunItem(@Nullable ItemStack itemStack) {
        this.taczExtras$boltingGunItem = itemStack;
    }

    @Inject(method = "initialData()V", at = @At("TAIL"), remap = false)
    private void taczExtras$clearBoltItemOnReset(CallbackInfo ci) {
        this.taczExtras$boltingGunItem = null;
    }

    @Inject(method = "initialData(Z)V", at = @At("TAIL"), require = 0, remap = false)
    private void taczExtras$clearBoltItemOnPatchedReset(boolean keepBolt, CallbackInfo ci) {
        this.taczExtras$boltingGunItem = null;
    }
}
