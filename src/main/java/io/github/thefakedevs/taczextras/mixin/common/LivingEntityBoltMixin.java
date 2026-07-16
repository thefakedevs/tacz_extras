package io.github.thefakedevs.taczextras.mixin.common;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.entity.shooter.LivingEntityBolt;
import com.tacz.guns.entity.shooter.LivingEntityDrawGun;
import com.tacz.guns.entity.shooter.LivingEntityShoot;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;
import io.github.thefakedevs.taczextras.gameplay.BoltStateAccess;

import java.util.Optional;

@Mixin(value = LivingEntityBolt.class, remap = false)
public abstract class LivingEntityBoltMixin {
    @Shadow
    @Final
    private ShooterDataHolder data;
    @Shadow
    @Final
    private LivingEntityDrawGun draw;
    @Shadow
    @Final
    private LivingEntityShoot shoot;
    @Shadow
    @Final
    private LivingEntity shooter;

    /**
     * @author TACZ Extras
     * @reason Preserve the historical bolt-state patch behind a runtime config flag.
     */
    @Overwrite
    public void bolt() {
        BoltStateAccess extension = (BoltStateAccess) this.data;
        if (this.data.currentGunItem == null) {
            extension.taczExtras$setBoltingGunItem(null);
            return;
        }
        ItemStack currentGunItem = this.data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof AbstractGunItem gun)) {
            extension.taczExtras$setBoltingGunItem(null);
            return;
        }

        ResourceLocation gunId = gun.getGunId(currentGunItem);
        TimelessAPI.getCommonGunIndex(gunId).ifPresent(gunIndex -> {
            if (this.shoot.getShootCoolDown() != 0
                    || this.data.reloadStateType.isReloading()
                    || this.draw.getDrawCoolDown() != 0
                    || this.data.isBolting) {
                return;
            }

            IGunOperator operator = IGunOperator.fromLivingEntity(this.shooter);
            Bolt boltType = gunIndex.getGunData().getBolt();
            boolean useInventoryAmmo = gun.useInventoryAmmo(currentGunItem);
            boolean hasAmmoInBarrel = gun.hasBulletInBarrel(currentGunItem) && boltType != Bolt.OPEN_BOLT;
            boolean hasInventoryAmmo = gun.hasInventoryAmmo(this.shooter, currentGunItem, operator.needCheckAmmo());
            boolean noAmmo = useInventoryAmmo && !hasInventoryAmmo
                    || !useInventoryAmmo && gun.getCurrentAmmoCount(currentGunItem) < 1;

            if (boltType != Bolt.MANUAL_ACTION || hasAmmoInBarrel || noAmmo) {
                return;
            }

            this.data.boltTimestamp = System.currentTimeMillis();
            this.data.isBolting = gun.startBolt(this.data, currentGunItem, this.shooter);

            boolean preserve = TaczExtrasConfig.COMMON.enableEnhancedBolting.get()
                    && TaczExtrasConfig.COMMON.preserveBoltOnGunSwap.get()
                    && this.data.isBolting;
            extension.taczExtras$setBoltingGunItem(preserve ? currentGunItem : null);
        });
    }

    /**
     * @author TACZ Extras
     * @reason Tick the preserved ItemStack while enhanced bolting is enabled.
     */
    @Overwrite
    public void tickBolt() {
        if (!this.data.isBolting) {
            return;
        }

        BoltStateAccess extension = (BoltStateAccess) this.data;
        boolean preserve = TaczExtrasConfig.COMMON.enableEnhancedBolting.get()
                && TaczExtrasConfig.COMMON.preserveBoltOnGunSwap.get();
        ItemStack preservedItem = preserve ? extension.taczExtras$getBoltingGunItem() : null;

        if (this.data.currentGunItem == null && preservedItem == null) {
            this.data.isBolting = false;
            extension.taczExtras$setBoltingGunItem(null);
            return;
        }

        ItemStack currentGunItem = preservedItem != null
                ? preservedItem
                : this.data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof AbstractGunItem gun)) {
            this.data.isBolting = false;
            extension.taczExtras$setBoltingGunItem(null);
            return;
        }

        ResourceLocation gunId = gun.getGunId(currentGunItem);
        Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gunId);
        this.data.isBolting = gunIndex
                .map(index -> gun.tickBolt(this.data, currentGunItem, this.shooter))
                .orElse(false);
        if (!this.data.isBolting) {
            extension.taczExtras$setBoltingGunItem(null);
        }
    }
}
