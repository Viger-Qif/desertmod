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

    // Для отслеживания позиции игрока и динамического спавна/деспауна
    private static double lastPlayerX = Double.NaN;
    private static double lastPlayerY = Double.NaN;
    private static double lastPlayerZ = Double.NaN;
    private static final double POSITION_THRESHOLD = 4.0; // Минимальное расстояние для проверки

    // Храним все NPC из конфига (даже те, что вне радиуса)
    private static final List<NpcEntry> allNpcEntries = new ArrayList<>();

    public static void syncFromConfig() {
        npcsEnabled = MyConfig.HANDLER.instance().enableNPC;
        // Загружаем все NPC из конфига при синхронизации
        allNpcEntries.clear();
        allNpcEntries.addAll(NpcDataManager.loadNpcs());
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

    /**
     * Проверяет, находится ли точка в радиусе прогрузки от игрока
     */
    public static boolean isInRenderRadius(MinecraftClient client, double x, double y, double z) {
        if (client.player == null) return false;

        int radius = MyConfig.HANDLER.instance().npcRenderRadius;
        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();

        double dx = x - playerX;
        double dy = y - playerY;
        double dz = z - playerZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        return dist <= radius;
    }

    public static void OnClientTick(MinecraftClient client) {
        ClientWorld world = client.world;

        // Если мира нет — сбрасываем флаг (игрок вышел в меню или сменил мир)
        if (world == null) {
            spawned = false;
            lastPlayerX = Double.NaN;
            lastPlayerY = Double.NaN;
            lastPlayerZ = Double.NaN;
            return;
        }

        if (!npcsEnabled) {
            // Если НПС отключены - очищаем всех
            despawnAll(world);
            return;
        }

        // Если ещё не заспавнили в этом мире — спавним
        if (!spawned) {
            spawnFromConfig(world, client);
            spawned = true;

            // Проверяем, изменилась ли позиция игрока достаточно для пересчёта
            var player = client.player;
            if (player != null) {
                double playerX = player.getX();
                double playerY = player.getY();
                double playerZ = player.getZ();

                boolean positionChanged = Double.isNaN(lastPlayerX) ||
                        Math.abs(playerX - lastPlayerX) > POSITION_THRESHOLD ||
                        Math.abs(playerY - lastPlayerY) > POSITION_THRESHOLD ||
                        Math.abs(playerZ - lastPlayerZ) > POSITION_THRESHOLD;

                if (positionChanged) {
                    // Обновляем позицию и переспавниваем НПС
                    lastPlayerX = playerX;
                    lastPlayerY = playerY;
                    lastPlayerZ = playerZ;
                    refreshAllNpcs();
                }
            }
        }

    }

    private static void spawnFromConfig(ClientWorld world, MinecraftClient client) {
        for (NpcEntry entry : allNpcEntries) {
            // Проверяем радиус перед спавном
            if (!isInRenderRadius(client, entry.x(), entry.y(), entry.z())) {
                continue;
            }

            Entity npc = spawnNpc(world, entry.getEntityType(), entry.x(), entry.y(), entry.z(), entry.yaw(), entry.animVariant());
            if (npc != null) spawnedNpcs.add(npc);
        }
    }

    @Nullable
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
     * Мгновенно обновляет всех НПС в мире.
     * Вызывается после изменений в конфиге.
     */
    public static void refreshAllNpcs() {
        var client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        ClientWorld world = client.world;
        var player = client.player;

        // ✅ Удаляем только тех НПС, которые сейчас вне радиуса
        // Оставляем тех, кто всё ещё в радиусе, чтобы не сбрасывать анимацию
        spawnedNpcs.removeIf(npc -> {
            if (npc == null) return true;

            double dx = npc.getX() - player.getX();
            double dy = npc.getY() - player.getY();
            double dz = npc.getZ() - player.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            int radius = MyConfig.HANDLER.instance().npcRenderRadius;

            if (dist > radius) {
                npc.discard();
                return true; // Удаляем из списка
            }
            return false; // Оставляем NPC
        });

        // ✅ Спавним новых НПС, которых ещё нет в радиусе
        for (NpcEntry npcData : allNpcEntries) {
            // Пропускаем если NPC уже заспавлен
            boolean alreadySpawned = spawnedNpcs.stream()
                    .anyMatch(npc -> npc != null &&
                            Math.abs(npc.getX() - npcData.x()) < 0.5 &&
                            Math.abs(npc.getY() - npcData.y()) < 0.5 &&
                            Math.abs(npc.getZ() - npcData.z()) < 0.5);

            if (alreadySpawned) continue;

            // Проверяем радиус
            double dx = npcData.x() - player.getX();
            double dy = npcData.y() - player.getY();
            double dz = npcData.z() - player.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            int radius = MyConfig.HANDLER.instance().npcRenderRadius;

            if (dist <= radius) {
                EntityType<? extends Entity> type = npcData.getEntityType();

                if (type != null) {
                    Entity npc = spawnNpc(
                            world,
                            type,
                            npcData.x(),
                            npcData.y(),
                            npcData.z(),
                            npcData.yaw(),
                            npcData.animVariant()
                    );

                    if (npc != null) {
                        spawnedNpcs.add(npc);
                    }
                }
            }
        }

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
