package io.github.thefakedevs.taczextras.gameplay;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;
import io.github.thefakedevs.taczextras.registry.ModParticles;
import io.github.thefakedevs.taczextras.registry.ModSounds;

public final class BulletImpactEffects {
    private BulletImpactEffects() {
    }

    public static void apply(EntityKineticBullet bullet, BlockHitResult result) {
        if (!TaczExtrasConfig.COMMON.enableBulletImpactPatch.get()) {
            return;
        }
        if (!(bullet.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos blockPos = result.getBlockPos();
        BlockState blockState = serverLevel.getBlockState(blockPos);
        Vec3 hitPos = result.getLocation();

        if (isWaterImpact(serverLevel, blockPos, blockState)) {
            if (TaczExtrasConfig.COMMON.enableWaterImpactEffects.get()) {
                playWaterImpact(serverLevel, hitPos);
            }
            return;
        }

        SoundType soundType = blockState.getSoundType(serverLevel, blockPos, bullet);
        boolean metalImpact = isMetal(soundType);
        if (TaczExtrasConfig.COMMON.enableBulletHitSounds.get()) {
            playMaterialImpact(serverLevel, hitPos, soundType, metalImpact);
        }
        if (TaczExtrasConfig.COMMON.enableBulletHitParticles.get()) {
            if (metalImpact) {
                spawnMetalSparks(serverLevel, hitPos, result.getDirection());
            } else {
                spawnBlockParticles(serverLevel, hitPos, blockState, result.getDirection());
            }
        }
    }

    public static boolean isWaterImpact(ServerLevel level, BlockPos pos, BlockState state) {
        return state.getFluidState().is(FluidTags.WATER)
                || level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    private static void playMaterialImpact(ServerLevel level, Vec3 hitPos,
                                           SoundType soundType, boolean metalImpact) {
        SoundEvent customSound = metalImpact
                ? ModSounds.BULLET_HIT_METAL.get()
                : ModSounds.BULLET_HIT_GENERIC.get();

        float multiplier = TaczExtrasConfig.COMMON.bulletHitSoundVolume.get().floatValue();
        float pitch = 0.8f + level.random.nextFloat() * 0.4f;
        float volume = (0.8f + level.random.nextFloat() * 0.2f) * multiplier;

        level.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                soundType.getHitSound(), SoundSource.BLOCKS, volume * 0.7f, pitch);
        level.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                customSound, SoundSource.BLOCKS, volume, pitch);
    }

    private static boolean isMetal(SoundType soundType) {
        return soundType == SoundType.METAL
                || soundType == SoundType.ANVIL
                || soundType == SoundType.CHAIN
                || soundType == SoundType.COPPER
                || soundType == SoundType.NETHERITE_BLOCK;
    }

    private static void spawnBlockParticles(ServerLevel level, Vec3 hitPos,
                                            BlockState blockState, Direction hitDirection) {
        int count = TaczExtrasConfig.COMMON.bulletHitParticleCount.get();
        if (count <= 0 || blockState.isAir()) {
            return;
        }

        Vec3 normal = Vec3.atLowerCornerOf(hitDirection.getNormal()).scale(0.05);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                hitPos.x + normal.x,
                hitPos.y + normal.y,
                hitPos.z + normal.z,
                count,
                0.12,
                0.12,
                0.12,
                0.08
        );
    }

    private static void spawnMetalSparks(ServerLevel level, Vec3 hitPos, Direction hitDirection) {
        int baseCount = TaczExtrasConfig.COMMON.bulletHitParticleCount.get();
        if (baseCount <= 0) {
            return;
        }

        int count = Math.min(baseCount * 2, 64);
        Vec3 normal = Vec3.atLowerCornerOf(hitDirection.getNormal());
        Vec3 origin = hitPos.add(normal.scale(0.035));
        for (int i = 0; i < count; i++) {
            double outwardSpeed = 0.09 + level.random.nextDouble() * 0.16;
            double spread = 0.075;
            double xSpeed = normal.x * outwardSpeed + level.random.nextGaussian() * spread;
            double ySpeed = normal.y * outwardSpeed + level.random.nextGaussian() * spread + 0.035;
            double zSpeed = normal.z * outwardSpeed + level.random.nextGaussian() * spread;
            level.sendParticles(
                    ModParticles.METAL_SPARK.get(),
                    origin.x, origin.y, origin.z,
                    0,
                    xSpeed, ySpeed, zSpeed,
                    1.0
            );
        }
    }

    private static void playWaterImpact(ServerLevel level, Vec3 hitPos) {
        int count = Math.max(TaczExtrasConfig.COMMON.bulletHitParticleCount.get(), 1);
        level.sendParticles(ParticleTypes.SPLASH,
                hitPos.x, hitPos.y, hitPos.z,
                count * 2, 0.2, 0.1, 0.2, 0.12);
        level.sendParticles(ParticleTypes.BUBBLE,
                hitPos.x, hitPos.y, hitPos.z,
                count, 0.1, 0.1, 0.1, 0.04);

        if (TaczExtrasConfig.COMMON.enableBulletHitSounds.get()) {
            float multiplier = TaczExtrasConfig.COMMON.bulletHitSoundVolume.get().floatValue();
            float pitch = 0.9f + level.random.nextFloat() * 0.2f;
            level.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                    ModSounds.BULLET_HIT_WATER.get(), SoundSource.BLOCKS,
                    multiplier * (0.8f + level.random.nextFloat() * 0.2f), pitch);
        }
    }
}
