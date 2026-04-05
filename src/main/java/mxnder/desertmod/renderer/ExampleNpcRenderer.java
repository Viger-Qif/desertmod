package mxnder.desertmod.renderer;

import mxnder.desertmod.MyConfig;
import mxnder.desertmod.entity.ExampleNpcEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

// Рендерер для Example NPC (с проверкой конфига)
public class ExampleNpcRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<ExampleNpcEntity, R> {

    // Настройки тени: радиус 0.41, прозрачность 0.56 (изменить значения)
    public ExampleNpcRenderer(EntityRendererFactory.Context context, EntityType<? extends ExampleNpcEntity> entityType) {
        super(context, entityType);
    }

    @Override
    protected float getShadowRadius(R state) {
        return 0.41f;
    }

    @Override
    protected float getShadowOpacity(R state) {
        return 0.56f;
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


