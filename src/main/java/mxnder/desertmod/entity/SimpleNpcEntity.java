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
import mxnder.desertmod.MyConfig;

public class SimpleNpcEntity extends PathAwareEntity implements GeoEntity {

    private String animVariant = "idle_hair";

    private RawAnimation lastAnim = null;
    private int lastTick = -1;

    public void setAnimVariant(String variant) {
        this.animVariant = variant;
    }

    public SimpleNpcEntity(EntityType<? extends SimpleNpcEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0D)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<SimpleNpcEntity> controller = new AnimationController<SimpleNpcEntity>(
                "idle", 0, this::idleAnimController);
        controllers.add(controller);
    }

    private static final RawAnimation IDLE_HAIR = RawAnimation.begin().thenLoop("idle_hair");
    private static final RawAnimation IDLE_HAT = RawAnimation.begin().thenLoop("idle_hat");
    private static final RawAnimation LEAN_1_HAIR = RawAnimation.begin().thenLoop("lean_1_hair");
    private static final RawAnimation TALK_HAIR = RawAnimation.begin().thenLoop("talk_hair");
    private static final RawAnimation SIT_1 = RawAnimation.begin().thenLoop("sit_1");

    private PlayState idleAnimController(AnimationTest<SimpleNpcEntity> controller) {

        // защита от лишних вызовов в один тик
        if (this.age == lastTick) {
            return PlayState.CONTINUE;
        }
        lastTick = this.age;

        // === НОВОЕ: замедление анимации если включена интеграция с радаром ===
        if (MyConfig.HANDLER.instance().useXareoRadar) {
            controller.setControllerSpeed(0.15f);
        } else {
            controller.setControllerSpeed(1.0f); // сброс к нормальной скорости
        }
        // ===================================================================

        RawAnimation targetAnim;

        switch (animVariant) {
            case "idle_hat":
                targetAnim = IDLE_HAT;
                break;
            case "lean_1_hair":
                targetAnim = LEAN_1_HAIR;
                break;
            case "talk_hair":
                targetAnim = TALK_HAIR;
                break;
            case "sit_1":
                targetAnim = SIT_1;
                break;
            default:
                targetAnim = IDLE_HAIR;
                break;
        }

        // ключевой фикс: не дергаем GeckoLib лишний раз
        if (!controller.isCurrentAnimation(targetAnim)) {
            controller.setAnimation(targetAnim);
            lastAnim = targetAnim;
        }

        return PlayState.CONTINUE;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
