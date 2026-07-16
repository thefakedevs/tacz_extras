package io.github.thefakedevs.taczextras.mixin.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gameplay.LocalPlayerCrawl;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ClientMessagePlayerCrawl;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;

@Mixin(value = LocalPlayerCrawl.class, remap = false)
public abstract class LocalPlayerCrawlMixin {
    @Shadow
    @Final
    private LocalPlayer player;
    @Shadow
    private boolean isCrawling;
    @Shadow
    private int crawCooldownTicks;

    @Shadow
    private void setCrawlPose() {
        throw new AssertionError();
    }

    /**
     * @author TACZ Extras
     * @reason Permit crawl without a held gun and make the cooldown configurable.
     */
    @Overwrite
    public void crawl(boolean isCrawl) {
        boolean patchEnabled = TaczExtrasConfig.COMMON.enableCrawlPatch.get();
        boolean allowWithoutGun = patchEnabled && TaczExtrasConfig.COMMON.crawlWithoutGun.get();
        if (!allowWithoutGun && !taczExtras$canUseCurrentGun(true)) {
            return;
        }
        if (this.crawCooldownTicks > 0
                || this.player.isSpectator()
                || this.player.isPassenger()
                || !this.player.onGround()) {
            return;
        }

        this.isCrawling = isCrawl;
        this.crawCooldownTicks = patchEnabled
                ? TaczExtrasConfig.COMMON.crawlCooldownTicks.get()
                : 10;
        NetworkHandler.CHANNEL.sendToServer(new ClientMessagePlayerCrawl(isCrawl));
    }

    /**
     * @author TACZ Extras
     * @reason Apply the configurable fall threshold without requiring a held gun.
     */
    @Overwrite
    public void tickCrawl() {
        if (this.crawCooldownTicks > 0) {
            this.crawCooldownTicks--;
        }

        boolean patchEnabled = TaczExtrasConfig.COMMON.enableCrawlPatch.get();
        boolean allowWithoutGun = patchEnabled && TaczExtrasConfig.COMMON.crawlWithoutGun.get();
        if (!allowWithoutGun && !taczExtras$canUseCurrentGun(false)) {
            taczExtras$cancelCrawl();
            return;
        }

        boolean invalidState = this.player.isSpectator()
                || this.player.isPassenger()
                || this.player.isSwimming();
        if (patchEnabled) {
            invalidState |= this.player.fallDistance > TaczExtrasConfig.COMMON.crawlFallDistance.get();
            if (!allowWithoutGun) {
                invalidState |= !this.player.onGround();
            }
        } else {
            invalidState |= !this.player.onGround();
        }

        if (invalidState) {
            taczExtras$cancelCrawl();
            return;
        }
        this.setCrawlPose();
    }

    private boolean taczExtras$canUseCurrentGun(boolean clientIndex) {
        ItemStack itemStack = this.player.getMainHandItem();
        if (!(itemStack.getItem() instanceof IGun gun) || !gun.isCanCrawl(itemStack)) {
            return false;
        }
        ResourceLocation gunId = gun.getGunId(itemStack);
        return clientIndex
                ? TimelessAPI.getClientGunIndex(gunId).isPresent()
                : TimelessAPI.getCommonGunIndex(gunId).isPresent();
    }

    private void taczExtras$cancelCrawl() {
        this.isCrawling = false;
        this.setCrawlPose();
    }
}
