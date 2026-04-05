package mxnder.desertmod.renderer;

import mxnder.desertmod.MyConfig;
import mxnder.desertmod.entity.SimpleNpcEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

// Рендерер для Simple NPC (с проверкой конфига)
public class SimpleNpcRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<SimpleNpcEntity, R> {

    // Настройки тени: радиус 0.37, прозрачность 0.48 (изменить значения)
    public SimpleNpcRenderer(EntityRendererFactory.Context context, EntityType<? extends SimpleNpcEntity> entityType) {
        super(context, entityType);
    }

    @Override
    protected float getShadowRadius(R state) {
        return 0.37f;
    }

    @Override
    protected float getShadowOpacity(R state) {
        return 0.48f;
    }

    // Рендер только если NPC включены в конфиге
    @Override
    public void render(R state, MatrixStack modelMatrix, CameraRenderState cameraRenderState, float partialTick) {
        if (!MyConfig.HANDLER.instance().enableNPC) {
            return;
        }
        super.render(state, modelMatrix, cameraRenderState, partialTick);
    }
}


