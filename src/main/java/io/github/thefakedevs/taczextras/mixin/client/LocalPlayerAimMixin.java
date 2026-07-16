package io.github.thefakedevs.taczextras.mixin.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gameplay.LocalPlayerAim;
import com.tacz.guns.client.gameplay.LocalPlayerDataHolder;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;

@Mixin(value = LocalPlayerAim.class, remap = false)
public abstract class LocalPlayerAimMixin {
    @Shadow
    @Final
    private LocalPlayerDataHolder data;
    @Shadow
    @Final
    private LocalPlayer player;

    @Shadow
    private void aimProgressCalculate(float alphaProgress) {
        throw new AssertionError();
    }

    @Shadow
    private float getAlphaProgress(GunData gunData) {
        throw new AssertionError();
    }

    /**
     * @author TACZ Extras
     * @reason Make reload-to-unaim behavior independent from a patched TACZ config field.
     */
    @Overwrite
    public void tickAimingProgress() {
        if (TaczExtrasConfig.CLIENT.reloadCancelsAim.get()
                && this.data.clientIsAiming
                && IGunOperator.fromLivingEntity(this.player)
                .getSynReloadState().getStateType().isReloading()) {
            this.data.clientIsAiming = false;
            IClientPlayerGunOperator.fromLocalPlayer(this.player).aim(false);
            return;
        }

        ItemStack mainHandItem = this.player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun gun)) {
            this.data.clientAimingProgress = 0;
            LocalPlayerDataHolder.oldAimingProgress = 0;
            return;
        }
        if (System.currentTimeMillis() - this.data.clientDrawTimestamp < 0) {
            this.data.clientIsAiming = false;
        }

        ResourceLocation gunId = gun.getGunId(mainHandItem);
        TimelessAPI.getCommonGunIndex(gunId).ifPresentOrElse(index -> {
            float alphaProgress = this.getAlphaProgress(index.getGunData());
            this.aimProgressCalculate(alphaProgress);
        }, () -> {
            this.data.clientAimingProgress = 0;
            LocalPlayerDataHolder.oldAimingProgress = 0;
        });
    }
}
