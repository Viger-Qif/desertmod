package mxnder.desertmod.models;

import mxnder.desertmod.Desertmod;
import mxnder.desertmod.entity.SimpleNpcEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class SimpleNpcModel extends GeoModel<SimpleNpcEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.of(Desertmod.MOD_ID, "desertmod:model/npc_simple_desert.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.of(Desertmod.MOD_ID, "desertmod:textures/entity/npc_simple_desert.png");
    }

    @Override
    public Identifier getAnimationResource(SimpleNpcEntity animatable) {
        return Identifier.of(Desertmod.MOD_ID, "desertmod:animations/npc_simple_desert.animation.json");
    }

}
