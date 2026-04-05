package mxnder.desertmod;

import mxnder.desertmod.entity.ExampleNpcEntity;
import mxnder.desertmod.entity.SimpleNpcEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Класс регистрации всех сущностей (NPC) мода Desertmod.
 * <p>
 * Содержит:
 * <ul>
 *   <li>Ключи реестра для типов сущностей (RegistryKey)</li>
 *   <li>Основные типы сущностей (EntityType) с параметрами спавна</li>
 *   <li>Метод регистрации сущностей в реестре Minecraft/Fabric</li>
 * </ul>
 * </p>
 * <p>
 * Каждая сущность регистрируется с уникальным идентификатором в формате
 * "desertmod:entity_name" и заданными параметрами (размер, группа спавна).
 * </p>
 */
public class ModEntities {

    /**
     * Ключ реестра для типа сущности Example NPC (лесоруб).
     * Используется для связи типа сущности с её рендером и другими компонентами.
     */
    public static final RegistryKey<EntityType<?>> EXAMPLE_NPC_KEY =
            RegistryKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    Identifier.of(Desertmod.MOD_ID, "npc_lamberjack")
            );

    /**
     * Ключ реестра для типа сущности Simple NPC (житель пустыни).
     * Используется для связи типа сущности с её рендером и другими компонентами.
     */
    public static final RegistryKey<EntityType<?>> SIMPLE_NPC_KEY =
            RegistryKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    Identifier.of(Desertmod.MOD_ID, "npc_simple_desert")
            );

    /**
     * Тип сущности для Example NPC (лесоруб).
     * Параметры:
     * <ul>
     *   <li>Группа спавна: MISC (разное)</li>
     *   <li>Размер: 0.6 x 1.8 блока (как у игрока)</li>
     * </ul>
     */
    public static final EntityType<ExampleNpcEntity> EXAMPLE_NPC = FabricEntityTypeBuilder
            .create(SpawnGroup.MISC, ExampleNpcEntity::new)
            .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
            .build(EXAMPLE_NPC_KEY);

    /**
     * Тип сущности для Simple NPC (житель пустыни).
     * Параметры:
     * <ul>
     *   <li>Группа спавна: MISC (разное)</li>
     *   <li>Размер: 0.6 x 1.8 блока (как у игрока)</li>
     * </ul>
     */
    public static final EntityType<SimpleNpcEntity> SIMPLE_NPC = FabricEntityTypeBuilder
            .create(SpawnGroup.MISC, SimpleNpcEntity::new)
            .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
            .build(SIMPLE_NPC_KEY);

    /**
     * Регистрирует все типы сущностей в реестре Minecraft.
     * <p>
     * Вызывается при инициализации мода для регистрации сущностей
     * в игровом реестре. Без этой регистрации сущности не смогут
     * существовать в мире игры.
     * </p>
     */
    public static void registerEntities() {
        Registry.register(Registries.ENTITY_TYPE, Identifier.of("desertmod", "npc_lamberjack"), EXAMPLE_NPC);
        Registry.register(Registries.ENTITY_TYPE, Identifier.of("desertmod", "npc_simple_desert"), SIMPLE_NPC);
    }
}
