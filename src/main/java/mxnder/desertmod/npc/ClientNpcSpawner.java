package mxnder.desertmod.npc;

import mxnder.desertmod.MyConfig;
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

// Спавн NPC на клиенте (при заходе в мир)
@Environment(EnvType.CLIENT)
public final class ClientNpcSpawner {

    // Флаг: были ли заспавнены NPC в текущей сессии
    private static boolean spawned = false;

    // Вызывается каждый тик клиента
    public static void OnClientTick(MinecraftClient client) {
        ClientWorld world = client.world;
        
        if (world == null) {
            spawned = false;
            return;
        }
        
        if (spawned) return;

        if (MyConfig.HANDLER.instance().enableNPC) {
            spawnAlongTheRiver(world);
            spawned = true;
        }
    }

    // Спавн всех NPC из списка
    private static void spawnAlongTheRiver(ClientWorld world) {
        for (ClientNpcEntry entry : AlongTheRiverNpcList.NPCS) {
            spawnNpc(world, entry);
        }
    }

    // Спавн одного NPC с настройками
    @Nullable
    private static Entity spawnNpc(ClientWorld world, ClientNpcEntry entry) {
        Entity npc = entry.type().create(world, SpawnReason.LOAD);
        if (npc == null) return null;

        npc.refreshPositionAndAngles(
                entry.x(), entry.y(), entry.z(),
                entry.yaw(), 0f
        );
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
        return npc;
    }
}
