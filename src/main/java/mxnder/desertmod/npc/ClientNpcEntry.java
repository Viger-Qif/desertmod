package mxnder.desertmod.npc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

/**
 * Класс-запись (record) для хранения данных клиентского NPC.
 * <p>
 * Содержит всю необходимую информацию для спавна декоративного NPC на клиенте:
 * <ul>
 *   <li>Тип сущности (EntityType)</li>
 *   <li>Координаты позиции (x, y, z)</li>
 *   <li>Угол поворота (yaw)</li>
 *   <li>Вариант анимации (animVariant)</li>
 * </ul>
 * </p>
 * <p>
 * Используется в {@link AlongTheRiverNpcList} для определения списка NPC,
 * которые появляются вдоль реки при загрузке мира.
 * </p>
 *
 * @param type       тип сущности для спавна
 * @param x          координата X позиции спавна
 * @param y          координата Y позиции спавна
 * @param z          координата Z позиции спавна
 * @param yaw        угол поворота сущности вокруг вертикальной оси (в градусах)
 * @param animVariant название варианта анимации (например, "idle_hair", "sit_1")
 *
 * @see ClientNpcSpawner класс, использующий эту запись для спавна NPC
 */
public record ClientNpcEntry(
    EntityType<? extends Entity> type,
    double x, double y, double z,
    float yaw,
    String animVariant
) {}
