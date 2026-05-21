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
import java.util.Objects;

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

    // Последний известный радиус рендера
    private static int lastKnownRadius = 50;

    // Таймер для периодической проверки изменений (каждые 10 тиков)
    private static int tickCounter = 0;

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
     * Обновляет видимость NPC на основе радиуса рендера.
     * Вызывается каждый тик для проверки изменений позиции игрока и радиуса.
     */
    public static void updateNpcVisibilityByRadius(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        tickCounter++;
        if (tickCounter < 10) return; // Проверяем только каждые 10 тиков для производительности
        tickCounter = 0;

        ClientWorld world = client.world;
        var player = client.player;
        int currentRadius = MyConfig.HANDLER.instance().npcRenderRadius;

        // Загружаем актуальный список NPC
        List<NpcEntry> npcs = NpcDataManager.loadNpcs();

        // Проверяем, изменилось ли количество NPC
        boolean npcListChanged = (npcs.size() != allNpcEntries.size());
        if (!npcListChanged) {
            for (int i = 0; i < npcs.size(); i++) {
                if (!npcs.get(i).equals(allNpcEntries.get(i))) {
                    npcListChanged = true;
                    break;
                }
            }
        }

        // Если список NPC изменился - полный рефреш
        if (npcListChanged) {
            allNpcEntries.clear();
            allNpcEntries.addAll(npcs);
            refreshAllNpcs();
            return;
        }

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        // Обновляем позицию
        lastPlayerX = playerX;
        lastPlayerY = playerY;
        lastPlayerZ = playerZ;

        // Проверяем каждого заспавленного NPC
        List<Entity> toRemove = new ArrayList<>();
        List<NpcEntry> toSpawn = new ArrayList<>();

        for (Entity npc : spawnedNpcs) {
            if (npc == null || !npc.isAlive()) {
                toRemove.add(npc);
                continue;
            }

            // Находим соответствующего NPC в списке по позиции
            NpcEntry entry = null;
            for (NpcEntry e : allNpcEntries) {
                if (Math.abs(e.x() - npc.getX()) < 0.5 &&
                        Math.abs(e.y() - npc.getY()) < 0.5 &&
                        Math.abs(e.z() - npc.getZ()) < 0.5) {
                    entry = e;
                    break;
                }
            }

            if (entry == null) {
                toRemove.add(npc);
                continue;
            }

            double dx = entry.x() - playerX;
            double dy = entry.y() - playerY;
            double dz = entry.z() - playerZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Удаляем NPC если он за пределами радиуса
            if (dist > currentRadius) {
                toRemove.add(npc);
            }
        }

        // Проверяем каких NPC нужно заспавнить
        for (NpcEntry entry : allNpcEntries) {
            boolean alreadySpawned = false;
            for (Entity npc : spawnedNpcs) {
                if (npc != null && npc.isAlive() &&
                        Math.abs(entry.x() - npc.getX()) < 0.5 &&
                        Math.abs(entry.y() - npc.getY()) < 0.5 &&
                        Math.abs(entry.z() - npc.getZ()) < 0.5) {
                    alreadySpawned = true;
                    break;
                }
            }

            if (!alreadySpawned) {
                double dx = entry.x() - playerX;
                double dy = entry.y() - playerY;
                double dz = entry.z() - playerZ;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                // Спавним NPC когда игрок приближается к радиусу
                if (dist <= currentRadius) {
                    toSpawn.add(entry);
                }
            }
        }

        // Удаляем NPC вне радиуса (с учётом буфера)
        for (Entity npc : toRemove) {
            if (npc != null) {
                world.removeEntity(npc.getId(), Entity.RemovalReason.DISCARDED);
                spawnedNpcs.remove(npc);
            }
        }

        // Спавним новые NPC в радиусе
        for (NpcEntry entry : toSpawn) {
            EntityType<? extends Entity> type = entry.getEntityType();
            if (type != null) {
                Entity npc = spawnNpc(
                        world,
                        type,
                        entry.x(),
                        entry.y(),
                        entry.z(),
                        entry.yaw(),
                        entry.animVariant()
                );
                if (npc != null) {
                    spawnedNpcs.add(npc);
                }
            }
        }
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
        }

    }

    private static void spawnFromConfig(ClientWorld world, MinecraftClient client) {
        List<NpcEntry> npcs = NpcDataManager.loadNpcs();
        allNpcEntries.clear();
        allNpcEntries.addAll(npcs);

        var player = MinecraftClient.getInstance().player;
        int radius = MyConfig.HANDLER.instance().npcRenderRadius;

        for (NpcEntry entry : npcs) {
            // Спавним только NPC в радиусе от игрока
            if (player != null) {
                double dx = entry.x() - player.getX();
                double dy = entry.y() - player.getY();
                double dz = entry.z() - player.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (dist > radius) {
                    continue; // Пропускаем NPC вне радиуса
                }
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

        // ✅ Удаляем всех НПС
        for (Entity npc : spawnedNpcs) {
            if (npc != null) {
                npc.discard();
            }
        }

        spawnedNpcs.clear();
        allNpcEntries.clear();

        // ✅ Спавним заново
        var npcs = NpcDataManager.loadNpcs();
        allNpcEntries.addAll(npcs);
        int radius = MyConfig.HANDLER.instance().npcRenderRadius;

        for (NpcEntry npcData : npcs) {
            // ✅ Получаем координаты игрока ОТДЕЛЬНО
            double playerX = player.getX();
            double playerY = player.getY();
            double playerZ = player.getZ();

            // ✅ Считаем расстояние
            double dx = npcData.x() - playerX;
            double dy = npcData.y() - playerY;
            double dz = npcData.z() - playerZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

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