package io.github.thefakedevs.taczextras.mixin.common;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.particles.BulletHoleOption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.thefakedevs.taczextras.compat.TaczCompatibility;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;
import io.github.thefakedevs.taczextras.gameplay.BulletImpactEffects;

@Mixin(value = EntityKineticBullet.class, remap = false)
public abstract class EntityKineticBulletMixin {
    @Shadow
    private ResourceLocation ammoId;
    @Shadow
    private ResourceLocation gunId;
    @Shadow
    private ResourceLocation gunDisplayId;
    @Shadow
    private boolean igniteBlock;

    @Inject(method = "onHitBlock", at = @At("TAIL"), remap = false)
    private void taczExtras$applyImpactPatchToBaseTacz(BlockHitResult result, Vec3 startVec,
                                                       Vec3 endVec, CallbackInfo ci) {
        if (!TaczCompatibility.hasNativeBulletImpactPatch()) {
            BulletImpactEffects.apply((EntityKineticBullet) (Object) this, result);
        }
    }

    @Inject(
            method = "handleBulletHitEffects",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void taczExtras$controlNativeImpactPatch(BlockHitResult result, Vec3 hitVec,
                                                     BlockPos pos, CallbackInfo ci) {
        EntityKineticBullet bullet = (EntityKineticBullet) (Object) this;
        if (!(bullet.level() instanceof ServerLevel serverLevel)) {
            ci.cancel();
            return;
        }

        BlockState hitState = serverLevel.getBlockState(pos);
        boolean patchEnabled = TaczExtrasConfig.COMMON.enableBulletImpactPatch.get();
        boolean waterImpact = BulletImpactEffects.isWaterImpact(serverLevel, pos, hitState)
                || hitState.getBlock() == Blocks.WATER;

        if (!patchEnabled || !waterImpact) {
            BulletHoleOption bulletHole = new BulletHoleOption(
                    result.getDirection(),
                    result.getBlockPos(),
                    this.ammoId.toString(),
                    this.gunId.toString(),
                    this.gunDisplayId.toString()
            );
            serverLevel.sendParticles(bulletHole, hitVec.x, hitVec.y, hitVec.z, 1, 0, 0, 0, 0);
        }

        if (patchEnabled) {
            BulletImpactEffects.apply(bullet, result);
        }
        if (this.igniteBlock) {
            serverLevel.sendParticles(ParticleTypes.LAVA, hitVec.x, hitVec.y, hitVec.z, 1, 0, 0, 0, 0);
        }
        ci.cancel();
    }
}
