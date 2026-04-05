package mxnder.desertmod.renderer;

import mxnder.desertmod.entity.ExampleNpcEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EntityType;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * Класс рендера для Example NPC (лесоруб).
 * <p>
 * Отвечает за отрисовку Example NPC в игровом мире. Наследуется от
 * {@link GeoEntityRenderer}, который предоставляет базовую функциональность
 * рендеринга для GeckoLib-моделей.
 * </p>
 * <p>
 * Основные настройки рендера:
 * <ul>
 *   <li>Радиус тени: 0.41 (для сравнения, у игрока - 0.5)</li>
 *   <li>Непрозрачность тени: 0.56</li>
 * </ul>
 * </p>
 *
 * @param <R> тип состояния рендера сущности, расширяющий LivingEntityRenderState и GeoRenderState
 * @see GeoEntityRenderer базовый класс для рендеров GeckoLib
 */
public class ExampleNpcRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<ExampleNpcEntity, R> {

    /**
     * Конструктор рендера Example NPC.
     *
     * @param context   контекст фабрики рендеров (предоставляется Minecraft)
     * @param entityType тип сущности, для которой создаётся рендер
     */
    public ExampleNpcRenderer(EntityRendererFactory.Context context, EntityType<? extends ExampleNpcEntity> entityType) {
        super(context, entityType);
    }

    /**
     * Возвращает радиус тени для этого NPC.
     * <p>
     * Значение немного меньше, чем у игрока (0.5), для более аккуратной тени.
     * </p>
     *
     * @param state состояние рендера
     * @return радиус тени (0.41)
     */
    @Override
    protected float getShadowRadius(R state) {
        return 0.41f; // 0.5 у игрока
    }

    /**
     * Возвращает непрозрачность тени для этого NPC.
     * <p>
     * Определяет, насколько тёмной будет тень под сущностью.
     * Более высокое значение делает тень заметнее.
     * </p>
     *
     * @param state состояние рендера
     * @return непрозрачность тени (0.56)
     */
    @Override
    protected float getShadowOpacity(R state) {
        return 0.56f;
    }
}


