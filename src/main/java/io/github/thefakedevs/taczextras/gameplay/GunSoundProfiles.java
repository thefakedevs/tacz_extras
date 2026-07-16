package io.github.thefakedevs.taczextras.gameplay;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;

import java.util.Map;

public final class GunSoundProfiles {
    private static final Map<ResourceLocation, Profile> BUNDLED = Map.of(
            id("ai_awp"), new Profile(1.5f, 1.3f),
            id("glock_17"), new Profile(0.5f, 0.3f),
            id("m1911"), new Profile(0.5f, 0.3f),
            id("sks_tactical"), new Profile(1.2f, 0.8f),
            id("ump45"), new Profile(0.7f, 0.5f)
    );

    private GunSoundProfiles() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("tacz", path);
    }

    public static Profile resolve(@Nullable ResourceLocation gunId, GunData gunData) {
        if (TaczExtrasConfig.COMMON.useBundledGunSoundProfiles.get() && gunId != null) {
            Profile bundled = BUNDLED.get(gunId);
            if (bundled != null) {
                return bundled;
            }
        }
        return new Profile(
                gunData.getFireSound().getFireMultiplier(),
                gunData.getFireSound().getSilenceMultiplier()
        );
    }

    @Nullable
    public static ResourceLocation getHeldGunId(LivingEntity entity) {
        ItemStack itemStack = entity.getMainHandItem();
        if (itemStack.getItem() instanceof IGun gun) {
            return gun.getGunId(itemStack);
        }
        return null;
    }

    public record Profile(float fire, float silence) {
    }
}
