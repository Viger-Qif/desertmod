package mxnder.desertmod.models;

import mxnder.desertmod.Desertmod;
import mxnder.desertmod.entity.ExampleNpcEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ExampleNpcModel extends GeoModel<ExampleNpcEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.of(Desertmod.MOD_ID, "desertmod:model/npc_lamberjack.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.of(Desertmod.MOD_ID, "desertmod:textures/entity/npc_lamberjack.png");
    }

    @Override
    public Identifier getAnimationResource(ExampleNpcEntity animatable) {
        return Identifier.of(Desertmod.MOD_ID, "desertmod:animations/npc_lamberjack.animation.json");
    }

}
