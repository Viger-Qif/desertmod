package mxnder.desertmod.npc;

import mxnder.desertmod.ModEntities;

import java.util.List;

// Список NPC для спавна в мире
public final class AlongTheRiverNpcList {

    // Координаты (X, Y, Z), угол поворота (yaw) и тип скина для каждого NPC
    public static final List<ClientNpcEntry> NPCS = List.of(
            new ClientNpcEntry(ModEntities.EXAMPLE_NPC, 5393.7, 64, -3276.35, 180f, "0"),
            new ClientNpcEntry(ModEntities.SIMPLE_NPC,  5380.5, 72, -3278.3, 0f, "lean_1_hair"),
            new ClientNpcEntry(ModEntities.SIMPLE_NPC,  5387.5, 64, -3291.5, 145f, "idle_hair"),
            new ClientNpcEntry(ModEntities.SIMPLE_NPC,  5386.5, 64, -3293.5, 330f, "talk_hair"),

            new ClientNpcEntry(ModEntities.SIMPLE_NPC,  5378.5, 64, -3303.5, 100f, "talk_hair"),
            new ClientNpcEntry(ModEntities.SIMPLE_NPC,  5375.5, 64, -3304.5, 280f, "talk_hair"),
            new ClientNpcEntry(ModEntities.SIMPLE_NPC,  5375.5, 64, -3302.9, 260f, "idle_hat")
    );
}
