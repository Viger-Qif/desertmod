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

public class SimpleNpcEntity extends PathAwareEntity implements GeoEntity {

    private String animVariant = "hair";

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
        controllers.add(new AnimationController<SimpleNpcEntity>(
                "idle", 0, this::idleAnimController)
        );
    }

    private static final RawAnimation IDLE_HAIR = RawAnimation.begin().thenLoop("idle_hair");
    private static final RawAnimation IDLE_HAT = RawAnimation.begin().thenLoop("idle_hat");
    private static final RawAnimation LEAN_1_HAIR = RawAnimation.begin().thenLoop("lean_1_hair");

    private PlayState idleAnimController(AnimationTest<SimpleNpcEntity> controller) {
        switch (animVariant)
        {
            case "idle_hair":
                controller.setAndContinue(IDLE_HAIR);
                break;
            case "idle_hat":
                controller.setAndContinue(IDLE_HAT);
                break;
            case "lean_1_hair":
                controller.setAndContinue(LEAN_1_HAIR);
                break;
        }

        return PlayState.CONTINUE;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
