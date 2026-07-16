package io.github.thefakedevs.taczextras.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import io.github.thefakedevs.taczextras.TaczExtras;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TaczExtras.MOD_ID);

    public static final RegistryObject<SoundEvent> BULLET_HIT_GENERIC = register("bullet_hit_generic");
    public static final RegistryObject<SoundEvent> BULLET_HIT_METAL = register("bullet_hit_metal");
    public static final RegistryObject<SoundEvent> BULLET_HIT_WATER = register("bullet_hit_water");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TaczExtras.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
