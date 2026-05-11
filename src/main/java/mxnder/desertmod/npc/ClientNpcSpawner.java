package mxnder.desertmod.npc;

import mxnder.desertmod.MyConfig;
import mxnder.desertmod.entity.SimpleNpcEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ClientNpcSpawner {

    // Флаг, чтобы спавн был только один раз за вход в мир
    private static boolean spawned = false;
    private static boolean npcsEnabled;

    public static void syncFromConfig() {
        npcsEnabled = MyConfig.HANDLER.instance().enableNPC;
    }

    // Храним ссылки на всех нпс, чтобы управлять ими
    private static final List<Entity> spawnedNpcs = new ArrayList<>();

    public static void setNpcsEnabled(boolean enabled) {
        npcsEnabled = enabled;

        // Если отключаем - удаляем всех заспавленных NPC
        if (!enabled) {
            ClientWorld world = MinecraftClient.getInstance().world;
            if (world != null) {
                for (Entity npc : spawnedNpcs) {
                    world.removeEntity(npc.getId(), Entity.RemovalReason.DISCARDED);
                }
            }
            spawnedNpcs.clear();
            spawned = false; // Разрешаем повторный спавн при включении
        }
    }

    public static boolean isNpcsEnabled() {
        return npcsEnabled;
    }

    public static void OnClientTick(MinecraftClient client) {
        ClientWorld world = client.world;

        // Если мира нет — сбрасываем флаг (игрок вышел в меню или сменил мир)
        if (world == null) {
            spawned = false;
            return;
        }

        // Если уже заспавнили в этом мире — ничего не делаем
        if (spawned) return;

        if (npcsEnabled) {
            spawnFromConfig(world);
            spawned = true;
        }

    }

    private static void spawnFromConfig(ClientWorld world) {
        List<NpcEntry> npcs = NpcDataManager.loadNpcs();
        for (NpcEntry entry : npcs) {
            Entity npc = spawnNpc(world, entry.getEntityType(), entry.x(), entry.y(), entry.z(), entry.yaw(), entry.animVariant());
            if (npc != null) spawnedNpcs.add(npc);
        }
    }

    @Nullable
    //private static Entity spawnNpc(ClientWorld world, ClientNpcEntry entry) {
        //Entity npc = entry.type().create(world, SpawnReason.LOAD);
    private static Entity spawnNpc(ClientWorld world, EntityType<? extends Entity> type,
                                   double x, double y, double z, float yaw, String animVariant) {
        if (type == null) return null;

        Entity npc = type.create(world, SpawnReason.LOAD);
        if (npc == null) return null;

        // Позиция и направление взгляда NPC
        npc.refreshPositionAndAngles(x, y, z, yaw, 0f);
        npc.setHeadYaw(yaw);

        // Вариант анимации применяется только к SimpleNpcEntity
        if (npc instanceof SimpleNpcEntity simple) {
            simple.setAnimVariant(animVariant);
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

    /**
     * Удаляет всех заспавненных NPC и очищает список.
     * Используется при перезагрузке конфига.
     */
    public static void despawnAll(ClientWorld world) {
        if (world != null) {
            for (Entity npc : spawnedNpcs) {
                world.removeEntity(npc.getId(), Entity.RemovalReason.DISCARDED);
            }
        }
        spawnedNpcs.clear();
        spawned = false;
    }
}
