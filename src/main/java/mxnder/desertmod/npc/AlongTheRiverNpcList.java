package mxnder.desertmod.npc;

import mxnder.desertmod.ModEntities;

import java.util.List;

public final class AlongTheRiverNpcList {

    /*
    ----- ВАРИАНТЫ АНИМАЦИ -----
    idle_hair
    idle_hat
    lean_1_hair
     */

    public static final List<ClientNpcEntry> NPCS = List.of(
            new ClientNpcEntry(ModEntities.EXAMPLE_NPC, 5393.7, 64, -3276.35, 180f, "0"),
            //new ClientNpcEntry(ModEntities.SIMPLE_NPC,  5395.7, 64, -3276.35, 180f, "idle_hat"),
            //new ClientNpcEntry(ModEntities.SIMPLE_NPC,  5397.7, 64, -3276.35, 180f, "idle_hair"),
            new ClientNpcEntry(ModEntities.SIMPLE_NPC,  5380.5, 72, -3279.3, 0f, "lean_1_hair")
    );
}
