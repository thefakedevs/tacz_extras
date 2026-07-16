package io.github.thefakedevs.taczextras.compat;

import com.tacz.guns.entity.EntityKineticBullet;

import java.util.Arrays;

public final class TaczCompatibility {
    private static final boolean NATIVE_BULLET_IMPACT_PATCH = Arrays.stream(EntityKineticBullet.class.getDeclaredMethods())
            .anyMatch(method -> method.getName().equals("handleBulletHitEffects"));

    private TaczCompatibility() {
    }

    public static boolean hasNativeBulletImpactPatch() {
        return NATIVE_BULLET_IMPACT_PATCH;
    }
}
