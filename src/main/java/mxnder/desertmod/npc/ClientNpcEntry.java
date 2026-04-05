package mxnder.desertmod.npc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

// Данные для спавна одного NPC (тип, координаты, поворот, анимация)
public record ClientNpcEntry(
    EntityType<? extends Entity> type,
    double x, double y, double z,
    float yaw,
    String animVariant
) {}
