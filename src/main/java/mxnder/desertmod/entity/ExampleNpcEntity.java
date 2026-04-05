package mxnder.desertmod.entity;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
 * Класс сущности Example NPC (лесоруб).
 * <p>
 * Представляет собой неигрового персонажа (NPC) для клиентского мода Desertmod.
 * NPC является декоративной сущностью без ИИ, которая:
 * <ul>
 *   <li>Имеет фиксированное здоровье (20 единиц)</li>
 *   <li>Не может двигаться (скорость = 0)</li>
 *   <li>Поддерживает анимации через GeckoLib</li>
 *   <li>Воспроизводит звуки и частицы при анимации рубки дерева</li>
 * </ul>
 * </p>
 * <p>
 * Особенность этого NPC - при проигрывании анимации рубки создаются:
 * <ul>
 *   <li>Частицы блоков дубового бревна</li>
 *   <li>Звук разрушения блока древесины</li>
 * </ul>
 * </p>
 *
 * @see PathAwareEntity базовый класс для мобов с навигацией
 * @see GeoEntity интерфейс GeckoLib для анимируемых сущностей
 */
public class ExampleNpcEntity extends PathAwareEntity implements GeoEntity {

    /**
     * Конструктор сущности Example NPC.
     *
     * @param type  тип сущности (EntityType)
     * @param world мир, в котором находится сущность
     */
    public ExampleNpcEntity(EntityType<? extends ExampleNpcEntity> type, World world) {
        super(type, world);
    }

    /**
     * Создаёт и возвращает контейнер атрибутов для Example NPC.
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
     * Создаёт частицы щепы и воспроизводит звук рубки дерева.
     * <p>
     * Вызывается на ключевых кадрах анимации для создания эффекта рубки.
     * Генерирует от 3 до 10 частиц дубового бревна вокруг NPC и
     * проигрывает звук разрушения древесины.
     * </p>
     */
    private void spawnChopParticles() {
        int kol_vo_part = random.nextInt(7) + 3; // Количество частиц: 3-10
        
        for (int i = 0; i < kol_vo_part; i++) {
            this.getEntityWorld().addParticleClient(
                    new BlockStateParticleEffect(
                            ParticleTypes.BLOCK,
                            Blocks.OAK_LOG.getDefaultState()
                    ),
                    getX() + 0.2,
                    getY() + 1,
                    getZ() - 1,
                    random.nextGaussian() * 0.02,
                    random.nextDouble() * 0.1 + 0.1,
                    random.nextGaussian() * 0.02);
        }
        
        // Воспроизведение звука только на клиенте
        if (this.getEntityWorld() instanceof ClientWorld clientWorld) {
            clientWorld.playSound(
                    MinecraftClient.getInstance().player,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_WOOD_BREAK,
                    SoundCategory.BLOCKS,
                    1.0f,
                    1.0f);
        }
    }

    /**
     * Регистрирует контроллеры анимаций для GeckoLib.
     * <p>
     * Контроллер анимации лесоруба включает обработчик звуковых ключевых кадров,
     * который вызывает создание частиц и звука при рубке.
     * </p>
     *
     * @param controllers регистратор контроллеров для добавления
     */
    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ExampleNpcEntity>(
                "idle", 0, this::idleAnimController)
                .setSoundKeyframeHandler(event -> {
                    spawnChopParticles();
                })
        );
    }

    /** Анимация бездействия (рубка дерева) */
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    /**
     * Контроллер анимации бездействия.
     * <p>
     * Устанавливает циклическое воспроизведение анимации idle.
     * </p>
     *
     * @param controller тестовый объект контроллера анимации
     * @return состояние PlayState.CONTINUE для продолжения воспроизведения
     */
    private PlayState idleAnimController(AnimationTest<ExampleNpcEntity> controller) {
        controller.setAndContinue(IDLE_ANIM);
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
