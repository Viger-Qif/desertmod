package mxnder.desertmod.renderer;

import mxnder.desertmod.entity.SimpleNpcEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class SimpleNpcRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<SimpleNpcEntity, R> {

    public SimpleNpcRenderer(EntityRendererFactory.Context context, EntityType<? extends SimpleNpcEntity> entityType) {
        super(context, entityType);
    }

    @Override
    protected float getShadowRadius(R state) {
        return 0.37f; // 0.5 у игрока
    }

    @Override
    protected float getShadowOpacity(R state) {
        return 0.48f;
    }
}


