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

public class ExampleNpcEntity extends PathAwareEntity implements GeoEntity {

    public ExampleNpcEntity(EntityType<? extends ExampleNpcEntity> type, World world) {
        super(type, world);
    }

    // АТРИБУТЫ
    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0D)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0D);
    }

    // ЧАСТИЦЫ И ЗВУК ДЛЯ ДРОВОСЕКА
    private void spawnChopParticles() {

        int kol_vo_part = random.nextInt(7) + 3;
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
        if (this.getEntityWorld() instanceof ClientWorld clientWorld) {
            clientWorld.playSound(
                    MinecraftClient.getInstance().player,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_WOOD_BREAK,
                    SoundCategory.BLOCKS,
                    1.0f,
                    1.0f );
        }
    }

    // АНИМАЦИИ
    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ExampleNpcEntity>(
                "idle", 0, this::idleAnimController)
                .setSoundKeyframeHandler(event -> {
                    spawnChopParticles();
                })
        );
    }

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");


    private PlayState idleAnimController(AnimationTest<ExampleNpcEntity> controller) {
        controller.setAndContinue(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    // КЭШ ГЕКОЛИБ
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
