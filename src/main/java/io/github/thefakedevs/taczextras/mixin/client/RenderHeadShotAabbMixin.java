package io.github.thefakedevs.taczextras.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.event.RenderHeadShotAABB;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.config.util.HeadShotAABBConfigRead;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;

@Mixin(value = RenderHeadShotAABB.class, remap = false)
public abstract class RenderHeadShotAabbMixin {
    /**
     * @author TACZ Extras
     * @reason Make the ReducedDebugInfo guard configurable on both patched and base TACZ builds.
     */
    @Overwrite
    @SubscribeEvent
    public static void onRenderEntity(RenderLivingEvent.Post<?, ?> event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes()
                || !RenderConfig.HEAD_SHOT_DEBUG_HITBOX.get()
                || minecraft.player == null) {
            return;
        }
        if (TaczExtrasConfig.CLIENT.hideHeadHitboxWithReducedDebugInfo.get()
                && minecraft.player.isReducedDebugInfo()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null) {
            return;
        }

        AABB aabb = HeadShotAABBConfigRead.getAABB(entityId);
        if (aabb == null) {
            float width = entity.getBbWidth();
            float eyeHeight = entity.getEyeHeight();
            aabb = new AABB(
                    -width / 2,
                    eyeHeight - 0.25,
                    -width / 2,
                    width / 2,
                    eyeHeight + 0.25,
                    width / 2
            ).inflate(0.01);
        }

        VertexConsumer buffer = event.getMultiBufferSource().getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(event.getPoseStack(), buffer, aabb, 1.0f, 1.0f, 0.0f, 1.0f);
    }
}
