package mxnder.desertmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.animation.state.AnimationTest;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Класс сущности Simple NPC (житель пустыни).
 * <p>
 * Представляет собой неигрового персонажа (NPC) для клиентского мода Desertmod.
 * NPC является декоративной сущностью без ИИ, которая:
 * <ul>
 *   <li>Имеет фиксированное здоровье (20 единиц)</li>
 *   <li>Не может двигаться (скорость = 0)</li>
 *   <li>Поддерживает анимации через GeckoLib</li>
 *   <li>Может отображать фразы над головой при взаимодействии</li>
 * </ul>
 * </p>
 * <p>
 * Доступные варианты анимаций:
 * <ul>
 *   <li>{@code idle_hair} - бездействие с волосами</li>
 *   <li>{@code idle_hat} - бездействие в шляпе</li>
 *   <li>{@code lean_1_hair} - опирание с волосами</li>
 *   <li>{@code talk_hair} - разговор с волосами</li>
 *   <li>{@code sit_1} - сидение</li>
 * </ul>
 * </p>
 *
 * @see PathAwareEntity базовый класс для мобов с навигацией
 * @see GeoEntity интерфейс GeckoLib для анимируемых сущностей
 */
public class SimpleNpcEntity extends PathAwareEntity implements GeoEntity {

    /**
     * Текущий вариант анимации для этого NPC.
     * Определяет, какая анимация будет проигрываться.
     */
    private String animVariant = "idle_hair";

    /**
     * Конструктор сущности Simple NPC.
     *
     * @param type  тип сущности (EntityType)
     * @param world мир, в котором находится сущность
     */
    public SimpleNpcEntity(EntityType<? extends SimpleNpcEntity> type, World world) {
        super(type, world);
    }

    /**
     * Устанавливает вариант анимации для этого NPC.
     * <p>
     * Вызывается при спавне NPC для задания начальной позы/анимации.
     * </p>
     *
     * @param variant название варианта анимации (например, "idle_hair", "sit_1")
     */
    public void setAnimVariant(String variant) {
        this.animVariant = variant;
    }

    /**
     * Создаёт и возвращает контейнер атрибутов для Simple NPC.
     * <p>
     * Атрибуты определяют базовые характеристики сущности:
     * <ul>
     *   <li>Максимальное здоровье: 20 единиц (как у игрока)</li>
     *   <li>Скорость передвижения: 0 (NPC не двигается)</li>
     * </ul>
     * </p>
     *
     * @return builder контейнера атрибутов с настроенными параметрами
     */
    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0D)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0D);
    }

    /**
     * Регистрирует контроллеры анимаций для GeckoLib.
     * <p>
     * Контроллер управляет проигрыванием анимаций на основе текущего
     * состояния сущности и выбранного варианта анимации.
     * </p>
     *
     * @param controllers регистратор контроллеров для добавления
     */
    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<SimpleNpcEntity>(
                "idle", 0, this::idleAnimController)
        );
    }

    // === Определения анимаций ===
    /** Анимация бездействия с волосами */
    private static final RawAnimation IDLE_HAIR = RawAnimation.begin().thenLoop("idle_hair");
    /** Анимация бездействия в шляпе */
    private static final RawAnimation IDLE_HAT = RawAnimation.begin().thenLoop("idle_hat");
    /** Анимация опирания с волосами */
    private static final RawAnimation LEAN_1_HAIR = RawAnimation.begin().thenLoop("lean_1_hair");
    /** Анимация разговора с волосами */
    private static final RawAnimation TALK_HAIR = RawAnimation.begin().thenLoop("talk_hair");
    /** Анимация сидения */
    private static final RawAnimation SIT_1 = RawAnimation.begin().thenLoop("sit_1");

    /**
     * Контроллер анимации бездействия.
     * <p>
     * Выбирает соответствующую анимацию на основе текущего варианта ({@link #animVariant}).
     * Если вариант не распознан, используется анимация по умолчанию ({@code idle_hair}).
     * </p>
     *
     * @param controller тестовый объект контроллера анимации
     * @return состояние PlayState.CONTINUE для продолжения воспроизведения
     */
    private PlayState idleAnimController(AnimationTest<SimpleNpcEntity> controller) {
        switch (animVariant) {
            case "idle_hair":
                controller.setAndContinue(IDLE_HAIR);
                break;
            case "idle_hat":
                controller.setAndContinue(IDLE_HAT);
                break;
            case "lean_1_hair":
                controller.setAndContinue(LEAN_1_HAIR);
                break;
            case "talk_hair":
                controller.setAndContinue(TALK_HAIR);
                break;
            case "sit_1":
                controller.setAndContinue(SIT_1);
                break;
            default:
                controller.setAndContinue(IDLE_HAIR);
        }

        return PlayState.CONTINUE;
    }

    /**
     * Кэш экземпляра анимации GeckoLib.
     * Используется для оптимизации доступа к анимационным данным.
     */
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
