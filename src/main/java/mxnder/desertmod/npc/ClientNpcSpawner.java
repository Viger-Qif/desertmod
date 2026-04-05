package mxnder.desertmod.npc;

import mxnder.desertmod.entity.SimpleNpcEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ClientNpcSpawner {

    // Флаг, чтобы спавн был только один раз за сессию
    private static boolean spawned = false;
    private static boolean npcsEnabled = true;

    // Храним ссылки на всех нпс, чтобы управлять ими
    //private static final List<Entity> spawnedNpcs = new ArrayList<>();

    public static void OnClientTick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || spawned) return;

        if (npcsEnabled == true) {
            spawnAlongTheRiver(world);
            spawned = true;
        }

    }

    private static void spawnAlongTheRiver(ClientWorld world) {
        for (ClientNpcEntry entry : AlongTheRiverNpcList.NPCS) {
            Entity npc = spawnNpc(world, entry);
            //if (npc != null) spawnedNpcs.add(npc); // Сохраняем ссылку
        }
    }

    @Nullable
    private static Entity spawnNpc(ClientWorld world, ClientNpcEntry entry) {
        Entity npc = entry.type().create(world, SpawnReason.LOAD);
        if (npc == null) return null;

        // Позиция и направление взгляда NPC
        npc.refreshPositionAndAngles(
                entry.x(), entry.y(), entry.z(),
                entry.yaw(), 0f
        );
        npc.setHeadYaw(entry.yaw());

        // Вариант анимации применяется только к SimpleNpcEntity
        if (npc instanceof SimpleNpcEntity simple) {
            simple.setAnimVariant(entry.animVariant());
        }


        // Клиентские NPC должны быть "декоративными"
        npc.setNoGravity(true);
        npc.setSilent(true);
        npc.setInvulnerable(true);
        if (npc instanceof MobEntity mob) {
            mob.setAiDisabled(true);
        }

        world.addEntity(npc);
        return npc;
    }

}
