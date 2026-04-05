package mxnder.desertmod.models;

import mxnder.desertmod.Desertmod;
import mxnder.desertmod.entity.SimpleNpcEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

// Модель простого NPC (GeoModel)
public class SimpleNpcModel extends GeoModel<SimpleNpcEntity> {

    // Путь к 3D-модели (изменить путь к .geo.json)
    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.of(Desertmod.MOD_ID, "geckolib/models/npc_simple_desert.geo.json");
    }

    // Путь к текстуре (изменить путь к .png)
    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.of(Desertmod.MOD_ID, "textures/npc_simple_desert.png");
    }

    // Путь к анимациям (изменить путь к .animation.json)
    @Override
    public Identifier getAnimationResource(SimpleNpcEntity animatable) {
        return Identifier.of(Desertmod.MOD_ID, "npc_simple_desert.animation.json");
    }
}
