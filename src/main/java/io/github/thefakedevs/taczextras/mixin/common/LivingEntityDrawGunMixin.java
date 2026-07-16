package io.github.thefakedevs.taczextras.mixin.common;

import com.tacz.guns.entity.shooter.LivingEntityDrawGun;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import io.github.thefakedevs.taczextras.compat.ScriptDataCompat;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;
import io.github.thefakedevs.taczextras.gameplay.BoltStateAccess;

@Mixin(value = LivingEntityDrawGun.class, remap = false)
public abstract class LivingEntityDrawGunMixin {
    @Redirect(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tacz/guns/entity/shooter/ShooterDataHolder;initialData()V",
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private void taczExtras$resetBaseState(ShooterDataHolder holder) {
        taczExtras$resetState(holder);
    }

    @Redirect(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tacz/guns/entity/shooter/ShooterDataHolder;initialData(Z)V",
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private void taczExtras$resetPatchedState(ShooterDataHolder holder, boolean keepBolt) {
        taczExtras$resetState(holder);
    }

    private static void taczExtras$resetState(ShooterDataHolder holder) {
        BoltStateAccess extension = (BoltStateAccess) holder;
        boolean keepBolt = TaczExtrasConfig.COMMON.enableEnhancedBolting.get()
                && TaczExtrasConfig.COMMON.preserveBoltOnGunSwap.get()
                && holder.isBolting;

        long boltTimestamp = holder.boltTimestamp;
        ItemStack boltingGunItem = extension.taczExtras$getBoltingGunItem();
        Object scriptData = ScriptDataCompat.read(holder);

        holder.initialData();

        if (keepBolt) {
            holder.boltTimestamp = boltTimestamp;
            holder.isBolting = true;
            extension.taczExtras$setBoltingGunItem(boltingGunItem);
            ScriptDataCompat.write(holder, scriptData);
        } else {
            extension.taczExtras$setBoltingGunItem(null);
        }
    }
}
