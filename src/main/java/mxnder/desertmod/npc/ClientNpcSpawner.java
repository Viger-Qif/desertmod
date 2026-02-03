package mxnder.desertmod.npc;

import mxnder.desertmod.entity.SimpleNpcEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;

@Environment(EnvType.CLIENT)
public final class ClientNpcSpawner {

    private static boolean spawned = false;

    public static void OnClientTick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || spawned) return;

        spawnAlongTheRiver(world);
        spawned = true;
    }

    private static void spawnAlongTheRiver(ClientWorld world) {
        for (ClientNpcEntry entry : AlongTheRiverNpcList.NPCS) {
            spawnNpc(world, entry);
        }
    }

    private static void spawnNpc(ClientWorld world, ClientNpcEntry entry) {
        Entity npc = entry.type().create(world, SpawnReason.LOAD);
        if (npc == null) return;

        npc.refreshPositionAndAngles(
                entry.x(), entry.y(), entry.z(),
                entry.yaw(), 0f
        );
        npc.setHeadYaw(entry.yaw());
        npc.setHeadYaw(entry.yaw());

        if (npc instanceof SimpleNpcEntity simple) {
            simple.setAnimVariant(entry.animVariant());
        }

        npc.setNoGravity(true);
        npc.setSilent(true);
        npc.setInvulnerable(true);
        if (npc instanceof MobEntity mob) {
            mob.setAiDisabled(true);
        }

        world.addEntity(npc);
    }
}
