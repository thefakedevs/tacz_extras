package io.github.thefakedevs.taczextras.mixin.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;
import io.github.thefakedevs.taczextras.gameplay.GunSoundProfiles;

@Mixin(value = SoundPlayManager.class, remap = false)
public abstract class SoundPlayManagerMixin {
    @Unique
    private static final String TACZ_EXTRAS$LEGACY_PLAY_SOUND =
            "Lcom/tacz/guns/client/sound/SoundPlayManager;playClientSound("
                    + "Lnet/minecraft/world/entity/Entity;"
                    + "Lnet/minecraft/resources/ResourceLocation;FFI)"
                    + "Lcom/tacz/guns/client/sound/GunSoundInstance;";

    @Unique
    private static final String TACZ_EXTRAS$TRACKED_PLAY_SOUND =
            "Lcom/tacz/guns/client/sound/SoundPlayManager;playClientSound("
                    + "Lnet/minecraft/world/entity/Entity;"
                    + "Lnet/minecraft/resources/ResourceLocation;FFIZIZZ)"
                    + "Lcom/tacz/guns/client/sound/GunSoundInstance;";

    @ModifyArg(
            method = "playShootSound",
            at = @At(value = "INVOKE", target = TACZ_EXTRAS$LEGACY_PLAY_SOUND, remap = false),
            index = 2,
            require = 0,
            remap = false
    )
    private static float taczExtras$adjustLegacyShootVolume(
            Entity entity, ResourceLocation soundId, float volume, float pitch, int distance
    ) {
        return taczExtras$adjustShootVolume(entity, volume);
    }

    @ModifyArg(
            method = "playShootSound",
            at = @At(value = "INVOKE", target = TACZ_EXTRAS$LEGACY_PLAY_SOUND, remap = false),
            index = 4,
            require = 0,
            remap = false
    )
    private static int taczExtras$adjustLegacyShootDistance(
            Entity entity, ResourceLocation soundId, float volume, float pitch, int distance
    ) {
        return taczExtras$adjustShootDistance(entity, distance);
    }

    @ModifyArg(
            method = "playSilenceSound",
            at = @At(value = "INVOKE", target = TACZ_EXTRAS$LEGACY_PLAY_SOUND, remap = false),
            index = 2,
            require = 0,
            remap = false
    )
    private static float taczExtras$adjustLegacySilenceVolume(
            Entity entity, ResourceLocation soundId, float volume, float pitch, int distance
    ) {
        return taczExtras$adjustSilenceVolume(entity, volume);
    }

    @ModifyArg(
            method = "playSilenceSound",
            at = @At(value = "INVOKE", target = TACZ_EXTRAS$LEGACY_PLAY_SOUND, remap = false),
            index = 4,
            require = 0,
            remap = false
    )
    private static int taczExtras$adjustLegacySilenceDistance(
            Entity entity, ResourceLocation soundId, float volume, float pitch, int distance
    ) {
        return taczExtras$adjustSilenceDistance(entity, distance);
    }

    @ModifyArg(
            method = "playShootSound",
            at = @At(value = "INVOKE", target = TACZ_EXTRAS$TRACKED_PLAY_SOUND, remap = false),
            index = 2,
            require = 0,
            remap = false
    )
    private static float taczExtras$adjustTrackedShootVolume(
            Entity entity, ResourceLocation soundId, float volume, float pitch, int distance,
            boolean mono, int concurrencyLimit, boolean trackEntity, boolean relative
    ) {
        return taczExtras$adjustShootVolume(entity, volume);
    }

    @ModifyArg(
            method = "playShootSound",
            at = @At(value = "INVOKE", target = TACZ_EXTRAS$TRACKED_PLAY_SOUND, remap = false),
            index = 4,
            require = 0,
            remap = false
    )
    private static int taczExtras$adjustTrackedShootDistance(
            Entity entity, ResourceLocation soundId, float volume, float pitch, int distance,
            boolean mono, int concurrencyLimit, boolean trackEntity, boolean relative
    ) {
        return taczExtras$adjustShootDistance(entity, distance);
    }

    @ModifyArg(
            method = "playSilenceSound",
            at = @At(value = "INVOKE", target = TACZ_EXTRAS$TRACKED_PLAY_SOUND, remap = false),
            index = 2,
            require = 0,
            remap = false
    )
    private static float taczExtras$adjustTrackedSilenceVolume(
            Entity entity, ResourceLocation soundId, float volume, float pitch, int distance,
            boolean mono, int concurrencyLimit, boolean trackEntity, boolean relative
    ) {
        return taczExtras$adjustSilenceVolume(entity, volume);
    }

    @ModifyArg(
            method = "playSilenceSound",
            at = @At(value = "INVOKE", target = TACZ_EXTRAS$TRACKED_PLAY_SOUND, remap = false),
            index = 4,
            require = 0,
            remap = false
    )
    private static int taczExtras$adjustTrackedSilenceDistance(
            Entity entity, ResourceLocation soundId, float volume, float pitch, int distance,
            boolean mono, int concurrencyLimit, boolean trackEntity, boolean relative
    ) {
        return taczExtras$adjustSilenceDistance(entity, distance);
    }

    @Unique
    private static float taczExtras$adjustShootVolume(Entity entity, float originalVolume) {
        GunSoundProfiles.Profile profile = taczExtras$resolveProfile(entity);
        return profile == null ? originalVolume : 0.8f * profile.fire();
    }

    @Unique
    private static int taczExtras$adjustShootDistance(Entity entity, int originalDistance) {
        GunSoundProfiles.Profile profile = taczExtras$resolveProfile(entity);
        if (profile == null) {
            return originalDistance;
        }
        int configuredDistance = TaczExtrasConfig.COMMON.fireSoundDistanceOverride.get();
        int baseDistance = configuredDistance > 0
                ? configuredDistance
                : GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get();
        return Math.max(0, (int) (baseDistance * profile.fire()));
    }

    @Unique
    private static float taczExtras$adjustSilenceVolume(Entity entity, float originalVolume) {
        GunSoundProfiles.Profile profile = taczExtras$resolveProfile(entity);
        return profile == null ? originalVolume : 0.6f * profile.silence();
    }

    @Unique
    private static int taczExtras$adjustSilenceDistance(Entity entity, int originalDistance) {
        GunSoundProfiles.Profile profile = taczExtras$resolveProfile(entity);
        return profile == null
                ? originalDistance
                : Math.max(0, (int) (GunConfig.DEFAULT_GUN_SILENCE_SOUND_DISTANCE.get()
                * profile.silence()));
    }

    @Unique
    private static GunSoundProfiles.Profile taczExtras$resolveProfile(Entity entity) {
        if (!TaczExtrasConfig.COMMON.enableGunSoundPatch.get()
                || !(entity instanceof LivingEntity livingEntity)) {
            return null;
        }
        ResourceLocation gunId = GunSoundProfiles.getHeldGunId(livingEntity);
        if (gunId == null) {
            return null;
        }
        GunData gunData = TimelessAPI.getClientGunIndex(gunId)
                .map(ClientGunIndex::getGunData)
                .orElse(null);
        return gunData == null ? null : GunSoundProfiles.resolve(gunId, gunData);
    }
}
