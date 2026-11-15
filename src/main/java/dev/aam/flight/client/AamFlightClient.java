package dev.aam.flight.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.aam.flight.ModEntities;
import dev.aam.flight.net.FirePayload;
import dev.aam.flight.net.RadarPayload;
import dev.aam.flight.net.ReleaseLockPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** 客户端入口：雷达接收 + 导弹渲染 + 手动解锁按键 */
public class AamFlightClient implements ClientModInitializer {

    public static volatile RadarPayload LATEST = null;
    private static boolean LAST_FIRING = false;

    public static final KeyMapping RELEASE_LOCK = new KeyMapping(
            "key.aamflight.release_lock",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KeyMapping.Category.GAMEPLAY
    );

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.AAM_MISSILE, AamMissileRenderer::new);

        KeyBindingHelper.registerKeyBinding(RELEASE_LOCK);

        ClientPlayNetworking.registerGlobalReceiver(RadarPayload.TYPE, (payload, context) ->
                context.client().execute(() -> LATEST = payload));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 轻机枪：按住右键状态同步喵
            if (client.player != null) {
                boolean down = client.options.keyUse.isDown();
                if (down != LAST_FIRING) {
                    LAST_FIRING = down;
                    ClientPlayNetworking.send(new FirePayload(down));
                }
            }
            while (RELEASE_LOCK.consumeClick()) {
                ClientPlayNetworking.send(new ReleaseLockPayload());
            }
        });
    }
}