package io.github.thefakedevs.taczextras.mixin.common;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.LivingEntityCrawl;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;

@Mixin(value = LivingEntityCrawl.class, remap = false)
public abstract class LivingEntityCrawlMixin {
    @Shadow
    @Final
    private LivingEntity shooter;
    @Shadow
    @Final
    private ShooterDataHolder data;

    @Shadow
    private void setCrawlPose() {
        throw new AssertionError();
    }

    /**
     * @author TACZ Extras
     * @reason Make the crawl rules configurable while retaining TACZ synchronization.
     */
    @Overwrite
    public void tickCrawling() {
        boolean patchEnabled = TaczExtrasConfig.COMMON.enableCrawlPatch.get();
        boolean allowWithoutGun = patchEnabled && TaczExtrasConfig.COMMON.crawlWithoutGun.get();

        if (!allowWithoutGun && !taczExtras$canUseCurrentGun()) {
            taczExtras$cancelCrawl();
            return;
        }

        boolean invalidState = this.shooter.isSpectator()
                || this.shooter.isPassenger()
                || this.shooter.isSwimming();

        if (patchEnabled) {
            invalidState |= this.shooter.fallDistance > TaczExtrasConfig.COMMON.crawlFallDistance.get();
            if (!allowWithoutGun) {
                invalidState |= !this.shooter.onGround();
            }
        } else {
            invalidState |= !this.shooter.onGround();
        }

        if (invalidState) {
            taczExtras$cancelCrawl();
            return;
        }
        this.setCrawlPose();
    }

    private boolean taczExtras$canUseCurrentGun() {
        if (this.data.currentGunItem == null) {
            return false;
        }
        ItemStack itemStack = this.data.currentGunItem.get();
        if (!(itemStack.getItem() instanceof IGun gun) || !gun.isCanCrawl(itemStack)) {
            return false;
        }
        ResourceLocation gunId = gun.getGunId(itemStack);
        return TimelessAPI.getCommonGunIndex(gunId).isPresent();
    }

    private void taczExtras$cancelCrawl() {
        this.data.isCrawling = false;
        this.setCrawlPose();
    }
}
