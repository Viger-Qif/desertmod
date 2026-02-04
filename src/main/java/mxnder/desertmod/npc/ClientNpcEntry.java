package mxnder.desertmod.npc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

// Данные для клиентского NPC: тип, позиция, поворот и вариант анимации
public record ClientNpcEntry(
    EntityType<? extends Entity> type,
    double x, double y, double z,
    float yaw,
    String animVariant,
    boolean canTalk // может нпс вообще говорить
) {}
