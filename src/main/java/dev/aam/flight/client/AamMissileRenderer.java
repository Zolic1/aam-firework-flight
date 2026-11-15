package dev.aam.flight.client;

import dev.aam.flight.AamMissile;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

/** AAM 导弹渲染：直接复用原版箭的外观喵 */
public class AamMissileRenderer extends ArrowRenderer<AamMissile, ArrowRenderState> {

    private static final Identifier TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png");

    public AamMissileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return TEXTURE;
    }
}
