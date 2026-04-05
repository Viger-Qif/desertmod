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

/**
 * Класс рендера для Simple NPC (житель пустыни).
 * <p>
 * Отвечает за отрисовку Simple NPC в игровом мире. Наследуется от
 * {@link GeoEntityRenderer}, который предоставляет базовую функциональность
 * рендеринга для GeckoLib-моделей.
 * </p>
 * <p>
 * Основные настройки рендера:
 * <ul>
 *   <li>Радиус тени: 0.37 (для сравнения, у игрока - 0.5)</li>
 *   <li>Непрозрачность тени: 0.48</li>
 * </ul>
 * </p>
 *
 * @param <R> тип состояния рендера сущности, расширяющий LivingEntityRenderState и GeoRenderState
 * @see GeoEntityRenderer базовый класс для рендеров GeckoLib
 */
public class SimpleNpcRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<SimpleNpcEntity, R> {

    /**
     * Конструктор рендера Simple NPC.
     *
     * @param context   контекст фабрики рендеров (предоставляется Minecraft)
     * @param entityType тип сущности, для которой создаётся рендер
     */
    public SimpleNpcRenderer(EntityRendererFactory.Context context, EntityType<? extends SimpleNpcEntity> entityType) {
        super(context, entityType);
    }

    /**
     * Возвращает радиус тени для этого NPC.
     * <p>
     * Значение меньше, чем у игрока (0.5), так как NPC может быть уже.
     * </p>
     *
     * @param state состояние рендера
     * @return радиус тени (0.37)
     */
    @Override
    protected float getShadowRadius(R state) {
        return 0.37f; // 0.5 у игрока
    }

    /**
     * Возвращает непрозрачность тени для этого NPC.
     * <p>
     * Определяет, насколько тёмной будет тень под сущностью.
     * </p>
     *
     * @param state состояние рендера
     * @return непрозрачность тени (0.48)
     */
    @Override
    protected float getShadowOpacity(R state) {
        return 0.48f;
    }
}


