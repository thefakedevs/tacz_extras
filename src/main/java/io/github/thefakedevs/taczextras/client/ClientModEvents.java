package io.github.thefakedevs.taczextras.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import io.github.thefakedevs.taczextras.TaczExtras;
import io.github.thefakedevs.taczextras.client.particle.MetalSparkParticle;
import io.github.thefakedevs.taczextras.registry.ModParticles;

@Mod.EventBusSubscriber(modid = TaczExtras.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.METAL_SPARK.get(), MetalSparkParticle.Provider::new);
    }
}
