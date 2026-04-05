package mxnder.desertmod.models;

import mxnder.desertmod.Desertmod;
import mxnder.desertmod.entity.ExampleNpcEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

// Модель примера NPC (GeoModel)
public class ExampleNpcModel extends GeoModel<ExampleNpcEntity> {

    // Путь к 3D-модели (изменить путь к .geo.json)
    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.of(Desertmod.MOD_ID, "geckolib/models/entity/npc_lamberjack.geo.json");
    }

    // Путь к текстуре (изменить путь к .png)
    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.of(Desertmod.MOD_ID, "textures/entity/npc_lamberjack.png");
    }

    // Путь к анимациям (изменить путь к .animation.json)
    @Override
    public Identifier getAnimationResource(ExampleNpcEntity animatable) {
        return Identifier.of(Desertmod.MOD_ID, "geckolib/animations/entity/npc_lamberjack.animation.json");
    }
}
