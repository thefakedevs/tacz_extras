package io.github.thefakedevs.taczextras.mixin.client;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.input.CrawlKey;
import com.tacz.guns.config.client.KeyConfig;
import com.tacz.guns.config.sync.SyncConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;

import static com.tacz.guns.util.InputExtraCheck.isInGame;

@Mixin(value = CrawlKey.class, remap = false)
public abstract class CrawlKeyMixin {
    /**
     * @author TACZ Extras
     * @reason Remove the input-side gun requirement when the crawl patch is enabled.
     */
    @Overwrite
    @SubscribeEvent
    public static void onCrawlPress(InputEvent.Key event) {
        if (!isInGame() || !CrawlKey.CRAWL_KEY.matches(event.getKey(), event.getScanCode())) {
            return;
        }
        if (!SyncConfig.ENABLE_CRAWL.get()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator() || player.isPassenger()
                || !(player instanceof IClientPlayerGunOperator operator)) {
            return;
        }

        boolean allowWithoutGun = TaczExtrasConfig.COMMON.enableCrawlPatch.get()
                && TaczExtrasConfig.COMMON.crawlWithoutGun.get();
        if (!allowWithoutGun) {
            if (!(player.getMainHandItem().getItem() instanceof IGun gun)) {
                return;
            }
            if (!gun.isCanCrawl(player.getMainHandItem())) {
                IClientPlayerGunOperator.fromLocalPlayer(player).crawl(false);
                return;
            }
        }

        boolean action = KeyConfig.HOLD_TO_CRAWL.get() || !operator.isCrawl();
        if (event.getAction() == GLFW.GLFW_PRESS) {
            IClientPlayerGunOperator.fromLocalPlayer(player).crawl(action);
        }
        if (KeyConfig.HOLD_TO_CRAWL.get() && event.getAction() == GLFW.GLFW_RELEASE) {
            IClientPlayerGunOperator.fromLocalPlayer(player).crawl(false);
        }
    }

    /**
     * @author TACZ Extras
     * @reason Apply the same rules to controller input and fix the original OR condition.
     */
    @Overwrite
    public static boolean onCrawlControllerPress(boolean isPress) {
        if (!isInGame() || !SyncConfig.ENABLE_CRAWL.get()) {
            return false;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator() || player.isPassenger()
                || !(player instanceof IClientPlayerGunOperator operator)) {
            return false;
        }

        boolean allowWithoutGun = TaczExtrasConfig.COMMON.enableCrawlPatch.get()
                && TaczExtrasConfig.COMMON.crawlWithoutGun.get();
        if (!allowWithoutGun && !IGun.mainHandHoldGun(player)) {
            return false;
        }

        boolean action = KeyConfig.HOLD_TO_CRAWL.get() || !operator.isCrawl();
        if (isPress) {
            IClientPlayerGunOperator.fromLocalPlayer(player).crawl(action);
            return true;
        }
        if (KeyConfig.HOLD_TO_CRAWL.get()) {
            IClientPlayerGunOperator.fromLocalPlayer(player).crawl(false);
            return true;
        }
        return false;
    }
}
