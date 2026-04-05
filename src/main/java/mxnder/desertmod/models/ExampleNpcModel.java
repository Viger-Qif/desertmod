package mxnder.desertmod.models;

import mxnder.desertmod.Desertmod;
import mxnder.desertmod.entity.ExampleNpcEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * Класс модели для Example NPC (лесоруб).
 * <p>
 * Определяет ресурсы GeckoLib для отрисовки Example NPC:
 * <ul>
 *   <li>3D-модель в формате JSON (geo.json)</li>
 *   <li>Текстуру в формате PNG</li>
 *   <li>Файл анимаций в формате JSON</li>
 * </ul>
 * </p>
 * <p>
 * Наследуется от {@link GeoModel}, который предоставляет базовую функциональность
 * для работы с GeckoLib.
 * </p>
 *
 * @param <ExampleNpcEntity> тип сущности, для которой предназначена модель
 * @see GeoModel базовый класс моделей GeckoLib
 */
public class ExampleNpcModel extends GeoModel<ExampleNpcEntity> {

    /**
     * Возвращает идентификатор ресурса 3D-модели.
     * <p>
     * Модель хранится по пути:
     * {@code assets/desertmod/geckolib/models/entity/npc_lamberjack.geo.json}
     * </p>
     *
     * @param state состояние рендера (не используется для статичной модели)
     * @return идентификатор файла модели
     */
    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.of(Desertmod.MOD_ID, "geckolib/models/entity/npc_lamberjack.geo.json");
    }

    /**
     * Возвращает идентификатор ресурса текстуры.
     * <p>
     * Текстура хранится по пути:
     * {@code assets/desertmod/textures/entity/npc_lamberjack.png}
     * </p>
     *
     * @param state состояние рендера (не используется для статичной текстуры)
     * @return идентификатор файла текстуры
     */
    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.of(Desertmod.MOD_ID, "textures/entity/npc_lamberjack.png");
    }

    /**
     * Возвращает идентификатор ресурса анимаций.
     * <p>
     * Файл анимаций хранится по пути:
     * {@code assets/desertmod/geckolib/animations/entity/npc_lamberjack.animation.json}
     * </p>
     *
     * @param animatable сущность, для которой запрашиваются анимации
     * @return идентификатор файла анимаций
     */
    @Override
    public Identifier getAnimationResource(ExampleNpcEntity animatable) {
        return Identifier.of(Desertmod.MOD_ID, "geckolib/animations/entity/npc_lamberjack.animation.json");
    }
}
