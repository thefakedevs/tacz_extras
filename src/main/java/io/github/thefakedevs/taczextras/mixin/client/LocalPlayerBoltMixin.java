package io.github.thefakedevs.taczextras.mixin.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.gameplay.LocalPlayerBolt;
import com.tacz.guns.client.gameplay.LocalPlayerDataHolder;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ClientMessagePlayerBoltGun;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;

@Mixin(value = LocalPlayerBolt.class, remap = false)
public abstract class LocalPlayerBoltMixin {
    @Shadow
    @Final
    private LocalPlayerDataHolder data;
    @Shadow
    @Final
    private LocalPlayer player;

    /**
     * @author TACZ Extras
     * @reason Route manual bolting through the configurable synchronization implementation.
     */
    @Overwrite
    public void bolt() {
        taczExtras$bolt(true);
    }

    @Unique
    private void taczExtras$bolt(boolean playFeedback) {
        if (this.data.clientStateLock || this.data.isBolting) {
            return;
        }

        ItemStack mainHandItem = this.player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun gun)) {
            return;
        }
        GunData gunData = TimelessAPI.getClientGunIndex(gun.getGunId(mainHandItem))
                .map(ClientGunIndex::getGunData)
                .orElse(null);
        if (gunData == null) {
            return;
        }

        TimelessAPI.getGunDisplay(mainHandItem).ifPresent(display -> {
            IGunOperator operator = IGunOperator.fromLivingEntity(this.player);
            Bolt boltType = gunData.getBolt();
            boolean useInventoryAmmo = gun.useInventoryAmmo(mainHandItem);
            boolean hasAmmoInBarrel = gun.hasBulletInBarrel(mainHandItem) && boltType != Bolt.OPEN_BOLT;
            boolean hasInventoryAmmo = gun.hasInventoryAmmo(this.player, mainHandItem, operator.needCheckAmmo());
            boolean noAmmo = useInventoryAmmo && !hasInventoryAmmo
                    || !useInventoryAmmo && gun.getCurrentAmmoCount(mainHandItem) < 1;

            if (boltType != Bolt.MANUAL_ACTION || hasAmmoInBarrel || noAmmo) {
                return;
            }

            this.data.lockState(IGunOperator::getSynIsBolting);
            this.data.isBolting = true;
            NetworkHandler.CHANNEL.sendToServer(new ClientMessagePlayerBoltGun());
            if (!playFeedback) {
                return;
            }

            AnimationStateMachine<?> animationStateMachine = display.getAnimationStateMachine();
            if (animationStateMachine != null) {
                SoundPlayManager.playBoltSound(this.player, display);
                animationStateMachine.trigger(GunAnimationConstant.INPUT_BOLT);
            }
        });
    }

    /**
     * @author TACZ Extras
     * @reason Retry a rejected or desynchronized bolt without replaying its feedback.
     */
    @Overwrite
    public void tickAutoBolt() {
        ItemStack mainHandItem = this.player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun gun)) {
            this.data.isBolting = false;
            return;
        }

        if (!TaczExtrasConfig.COMMON.enableEnhancedBolting.get()) {
            this.bolt();
            if (this.data.isBolting && gun.hasBulletInBarrel(mainHandItem)) {
                this.data.isBolting = false;
            }
            return;
        }

        IGunOperator operator = IGunOperator.fromLivingEntity(this.player);
        boolean retryBolt = false;
        if (this.data.isBolting
                && !this.data.clientStateLock
                && !operator.getSynIsBolting()
                && !gun.hasBulletInBarrel(mainHandItem)) {
            this.data.isBolting = false;
            retryBolt = true;
        }

        taczExtras$bolt(!retryBolt);
        if (this.data.isBolting && gun.hasBulletInBarrel(mainHandItem)) {
            this.data.isBolting = false;
        }
    }
}
