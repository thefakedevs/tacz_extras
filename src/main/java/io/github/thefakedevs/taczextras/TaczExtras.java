package io.github.thefakedevs.taczextras;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;
import io.github.thefakedevs.taczextras.registry.ModParticles;
import io.github.thefakedevs.taczextras.registry.ModSounds;

@Mod(TaczExtras.MOD_ID)
public final class TaczExtras {
    public static final String MOD_ID = "tacz_extras";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TaczExtras() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModParticles.register(modBus);
        ModSounds.register(modBus);
        TaczExtrasConfig.register();
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("TACZ Extras initialized");
    }
}
