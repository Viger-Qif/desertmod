package mxnder.desertmod.renderer;

import mxnder.desertmod.entity.ExampleNpcEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EntityType;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ExampleNpcRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<ExampleNpcEntity, R> {

    public ExampleNpcRenderer(EntityRendererFactory.Context context, EntityType<? extends ExampleNpcEntity> entityType) {
        super(context, entityType);
    }

    @Override
    protected float getShadowRadius(R state) {
        return 0.41f; // 0.5 у игрока
    }

    @Override
    protected float getShadowOpacity(R state) {
        return 0.56f;
    }
}


