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

// Пример сущности NPC (лumberjack с частицами и звуком)
public class ExampleNpcEntity extends PathAwareEntity implements GeoEntity {

    public ExampleNpcEntity(EntityType<? extends ExampleNpcEntity> type, World world) {
        super(type, world);
    }

    // Атрибуты: здоровье 20, скорость 0 (изменить значения)
    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0D)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0D);
    }

    // Спавн частиц дерева при анимации
    private void spawnChopParticles() {
        int kol_vo_part = random.nextInt(7) + 3;  // 3-10 частиц
        
        for (int i = 0; i < kol_vo_part; i++) {
            this.getEntityWorld().addParticleClient(
                    new BlockStateParticleEffect(
                            ParticleTypes.BLOCK,
                            Blocks.OAK_LOG.getDefaultState()  // Изменить блок
                    ),
                    getX() + 0.2,
                    getY() + 1,
                    getZ() - 1,
                    random.nextGaussian() * 0.02,
                    random.nextDouble() * 0.1 + 0.1,
                    random.nextGaussian() * 0.02);
        }
        
        if (this.getEntityWorld() instanceof ClientWorld clientWorld) {
            clientWorld.playSound(
                    MinecraftClient.getInstance().player,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_WOOD_BREAK,  // Изменить звук
                    SoundCategory.BLOCKS,
                    1.0f,  // Громкость
                    1.0f); // Питч
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ExampleNpcEntity>(
                "idle", 0, this::idleAnimController)
                .setSoundKeyframeHandler(event -> {
                    spawnChopParticles();
                })
        );
    }

    // Название анимации (должно совпадать с .animation.json)
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    // Контроллер анимации
    private PlayState idleAnimController(AnimationTest<ExampleNpcEntity> controller) {
        controller.setAndContinue(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
