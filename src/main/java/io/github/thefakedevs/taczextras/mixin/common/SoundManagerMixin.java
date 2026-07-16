package io.github.thefakedevs.taczextras.mixin.common;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.sound.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;
import io.github.thefakedevs.taczextras.gameplay.GunSoundProfiles;

@Mixin(value = SoundManager.class, remap = false)
public abstract class SoundManagerMixin {
    @Unique
    private static final ThreadLocal<Boolean> taczExtras$reentry = ThreadLocal.withInitial(() -> false);

    @Inject(method = "sendSoundToNearby", at = @At("HEAD"), cancellable = true, remap = false)
    private static void taczExtras$adjustThirdPersonGunSound(
            LivingEntity sourceEntity,
            int distance,
            ResourceLocation gunId,
            ResourceLocation gunDisplayId,
            String soundName,
            float volume,
            float pitch,
            CallbackInfo ci
    ) {
        if (taczExtras$reentry.get()) {
            return;
        }

        boolean shoot = SoundManager.SHOOT_3P_SOUND.equals(soundName);
        boolean silenced = SoundManager.SILENCE_3P_SOUND.equals(soundName);
        if (!shoot && !silenced) {
            return;
        }

        GunData gunData = TimelessAPI.getCommonGunIndex(gunId)
                .map(index -> index.getGunData())
                .orElse(null);
        if (gunData == null) {
            return;
        }

        boolean enabled = TaczExtrasConfig.COMMON.enableGunSoundPatch.get();
        GunSoundProfiles.Profile profile = GunSoundProfiles.resolve(gunId, gunData);
        float adjustedVolume = enabled
                ? (silenced ? 0.6f * profile.silence() : 0.8f * profile.fire())
                : 0.8f;
        int adjustedDistance = distance;
        int distanceOverride = TaczExtrasConfig.COMMON.fireSoundDistanceOverride.get();
        if (enabled && shoot && distanceOverride > 0) {
            adjustedDistance = Math.max(0, (int) (distanceOverride * profile.fire()));
        }

        if (Float.compare(volume, adjustedVolume) == 0 && distance == adjustedDistance) {
            return;
        }

        taczExtras$reentry.set(true);
        try {
            SoundManager.sendSoundToNearby(
                    sourceEntity,
                    adjustedDistance,
                    gunId,
                    gunDisplayId,
                    soundName,
                    adjustedVolume,
                    pitch
            );
        } finally {
            taczExtras$reentry.set(false);
        }
        ci.cancel();
    }
}
