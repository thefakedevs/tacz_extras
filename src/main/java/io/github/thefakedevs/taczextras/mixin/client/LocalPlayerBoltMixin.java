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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;

@Mixin(value = LocalPlayerBolt.class, remap = false)
public abstract class LocalPlayerBoltMixin {
    @Shadow
    @Final
    private LocalPlayerDataHolder data;
    @Shadow
    @Final
    private LocalPlayer player;

    @Inject(method = "bolt", at = @At("HEAD"), cancellable = true)
    private void taczExtras$retryDesynchronizedBolt(CallbackInfo ci) {
        if (!TaczExtrasConfig.COMMON.enableEnhancedBolting.get()) {
            return;
        }

        ItemStack mainHandItem = this.player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun gun)) {
            return;
        }

        IGunOperator operator = IGunOperator.fromLivingEntity(this.player);
        if (!this.data.isBolting
                || this.data.clientStateLock
                || operator.getSynIsBolting()
                || gun.hasBulletInBarrel(mainHandItem)) {
            return;
        }

        this.data.isBolting = false;
        taczExtras$bolt(false);
        ci.cancel();
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

}
