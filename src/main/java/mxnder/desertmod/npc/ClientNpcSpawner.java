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

/**
 * Класс управления спавном клиентских NPC.
 * <p>
 * Отвечает за создание декоративных NPC в мире при загрузке клиента.
 * NPC появляются один раз за сессию в заранее определённых координатах,
 * указанных в {@link AlongTheRiverNpcList}.
 * </p>
 * <p>
 * Особенности спавна:
 * <ul>
 *   <li>NPC являются декоративными (без ИИ, не двигаются)</li>
 *   <li>Установлена неуязвимость и бесшумность</li>
 *   <li>Гравитация отключена для стабильной позиции</li>
 *   <li>Поддерживают различные варианты анимаций</li>
 * </ul>
 * </p>
 * <p>
 * Спавн происходит только на клиенте (client-only мод) и контролируется
 * флагом {@code npcsEnabled}, который можно изменить через конфигурацию.
 * </p>
 *
 * @see ClientNpcEntry запись с данными для каждого NPC
 * @see AlongTheRiverNpcList список координат и параметров NPC
 */
@Environment(EnvType.CLIENT)
public final class ClientNpcSpawner {

    /**
     * Флаг, предотвращающий повторный спавн NPC за одну сессию.
     * После первого спавна устанавливается в {@code true}.
     */
    private static boolean spawned = false;

    /**
     * Флаг включения/выключения спавна NPC.
     * Управляется через конфигурацию мода ({@link MyConfig}).
     */
    private static boolean npcsEnabled = true;

    /**
     * Обработчик клиентского тика, вызываемый каждый кадр.
     * <p>
     * Проверяет возможность спавна и вызывает процедуру спавна NPC,
     * если мир загружен и спавн ещё не происходил.
     * </p>
     *
     * @param client экземпляр клиентского приложения Minecraft
     */
    public static void OnClientTick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || spawned) return;

        if (npcsEnabled) {
            spawnAlongTheRiver(world);
            spawned = true;
        }
    }

    /**
     * Спавнит всех NPC из списка "Вдоль реки".
     * <p>
     * Проходит по всем записям в {@link AlongTheRiverNpcList#NPCS}
     * и создаёт соответствующие сущности в мире.
     * </p>
     *
     * @param world клиентский мир для спавна сущностей
     */
    private static void spawnAlongTheRiver(ClientWorld world) {
        for (ClientNpcEntry entry : AlongTheRiverNpcList.NPCS) {
            Entity npc = spawnNpc(world, entry);
            //if (npc != null) spawnedNpcs.add(npc); // Сохраняем ссылку
        }
    }

    /**
     * Создаёт отдельного NPC в мире с заданными параметрами.
     * <p>
     * Настраивает сущность как декоративную:
     * <ul>
     *   <li>Устанавливает позицию и угол поворота</li>
     *   <li>Применяет вариант анимации (для SimpleNpcEntity)</li>
     *   <li>Отключает гравитацию, звуки и урон</li>
     *   <li>Отключает ИИ для мобов</li>
     * </ul>
     * </p>
     *
     * @param world клиентский мир для спавна
     * @param entry данные NPC (тип, позиция, анимация)
     * @return созданная сущность или {@code null} при ошибке
     */
    @Nullable
    private static Entity spawnNpc(ClientWorld world, ClientNpcEntry entry) {
        Entity npc = entry.type().create(world, SpawnReason.LOAD);
        if (npc == null) return null;

        // Установка позиции и направления взгляда NPC
        npc.refreshPositionAndAngles(
                entry.x(), entry.y(), entry.z(),
                entry.yaw(), 0f
        );
        npc.setHeadYaw(entry.yaw());

        // Применение варианта анимации только для SimpleNpcEntity
        if (npc instanceof SimpleNpcEntity simple) {
            simple.setAnimVariant(entry.animVariant());
        }

        // Настройка NPC как декоративной сущности
        npc.setNoGravity(true);      // Отключить гравитацию
        npc.setSilent(true);         // Отключить звуки
        npc.setInvulnerable(true);   // Сделать неуязвимым
        if (npc instanceof MobEntity mob) {
            mob.setAiDisabled(true); // Отключить ИИ
        }

        world.addEntity(npc);
        return npc;
    }
}
