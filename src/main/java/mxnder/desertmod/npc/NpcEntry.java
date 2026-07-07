package mxnder.desertmod.npc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import mxnder.desertmod.ModEntities;

/**
 * Данные для клиентского NPC (для редактора и JSON сохранения).
 * Содержит всю информацию необходимую для спавна и отображения NPC.
 */
@Environment(EnvType.CLIENT)
public record NpcEntry(
        String id,
        String typeKey,
        double x, double y, double z,
        float yaw,
        String animVariant
) {

    /**
     * Преобразует запись в реальную сущность для спавна в мире.
     *
     * @return EntityType соответствующий typeKey, или null если тип не найден
     */
    public EntityType<? extends Entity> getEntityType() {
        return switch (typeKey) {
            case "simple" -> ModEntities.SIMPLE_NPC;
            case "example" -> ModEntities.EXAMPLE_NPC;
            default -> null;
        };
    }

    /**
     * Список всех доступных типов NPC для выпадающего списка в UI.
     */
    public static final String[] AVAILABLE_TYPES = {"simple", "example"};

    /**
     * Список всех доступных анимаций для SIMPLE_NPC.
     */
    public static final String[] SIMPLE_ANIMATIONS = {
            "idle_hair", "idle_hat", "lean_1_hair", "talk_hair", "sit_1"
    };

    /**
     * Список всех доступных анимаций для EXAMPLE_NPC.
     */
    public static final String[] EXAMPLE_ANIMATIONS = {
            "idle"
    };

    /**
     * Получает список анимаций для выбранного типа NPC.
     *
     * @param typeKey Тип NPC
     * @return Массив доступных анимаций
     */
    public static String[] getAnimationsForType(String typeKey) {
        return switch (typeKey) {
            case "simple" -> SIMPLE_ANIMATIONS;
            case "example" -> EXAMPLE_ANIMATIONS;
            default -> new String[]{"idle"};
        };
    }
}