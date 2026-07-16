package io.github.thefakedevs.taczextras.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import io.github.thefakedevs.taczextras.TaczExtras;

public final class ModParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TaczExtras.MOD_ID);

    public static final RegistryObject<SimpleParticleType> METAL_SPARK =
            PARTICLES.register("metal_spark", () -> new SimpleParticleType(false));

    private ModParticles() {
    }

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
