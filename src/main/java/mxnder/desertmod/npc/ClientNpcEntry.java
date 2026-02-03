package mxnder.desertmod.npc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public record ClientNpcEntry(
    EntityType<? extends Entity> type,
    double x, double y, double z,
    float yaw,
    String animVariant
) {}
