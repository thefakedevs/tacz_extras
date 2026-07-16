package io.github.thefakedevs.taczextras.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class MetalSparkParticle extends SimpleAnimatedParticle {
    private static final float START_GREEN = 0.55f;
    private static final float START_BLUE = 0.03f;
    private static final float FADE_START = 0.58f;

    private MetalSparkParticle(ClientLevel level, double x, double y, double z,
                               double xSpeed, double ySpeed, double zSpeed,
                               SpriteSet sprites) {
        super(level, x, y, z, sprites, 0.45f);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.quadSize *= 0.65f + this.random.nextFloat() * 0.25f;
        this.lifetime = 9 + this.random.nextInt(7);
        this.hasPhysics = true;
        this.setSize(0.02f, 0.02f);
        this.setSpriteFromAge(sprites);
        this.updateAppearance();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }

        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
        this.updateAppearance();
    }

    private void updateAppearance() {
        float progress = Mth.clamp((float) this.age / Math.max(this.lifetime, 1), 0.0f, 1.0f);
        this.setColor(
                1.0f,
                Mth.lerp(progress, START_GREEN, 1.0f),
                Mth.lerp(progress, START_BLUE, 1.0f)
        );

        float alpha = progress <= FADE_START
                ? 1.0f
                : 1.0f - (progress - FADE_START) / (1.0f - FADE_START);
        this.setAlpha(Mth.clamp(alpha, 0.0f, 1.0f));
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new MetalSparkParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
