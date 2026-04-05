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

// Регистрация сущностей NPC (типы и размеры)
public class ModEntities {

    // Ключ типа для Example NPC (лumberjack)
    public static final RegistryKey<EntityType<?>> EXAMPLE_NPC_KEY =
            RegistryKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    Identifier.of(Desertmod.MOD_ID, "npc_lamberjack")
            );

    // Ключ типа для Simple NPC (desert)
    public static final RegistryKey<EntityType<?>> SIMPLE_NPC_KEY =
            RegistryKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    Identifier.of(Desertmod.MOD_ID, "npc_simple_desert")
            );

    // Тип Example NPC: размеры 0.6x1.8 (изменить в fixed(width, height))
    public static final EntityType<ExampleNpcEntity> EXAMPLE_NPC = FabricEntityTypeBuilder
            .create(SpawnGroup.MISC, ExampleNpcEntity::new)
            .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
            .build(EXAMPLE_NPC_KEY);

    // Тип Simple NPC: размеры 0.6x1.8 (изменить в fixed(width, height))
    public static final EntityType<SimpleNpcEntity> SIMPLE_NPC = FabricEntityTypeBuilder
            .create(SpawnGroup.MISC, SimpleNpcEntity::new)
            .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
            .build(SIMPLE_NPC_KEY);

    // Регистрация всех типов сущностей
    public static void registerEntities() {
        Registry.register(Registries.ENTITY_TYPE, Identifier.of("desertmod", "npc_lamberjack"), EXAMPLE_NPC);
        Registry.register(Registries.ENTITY_TYPE, Identifier.of("desertmod", "npc_simple_desert"), SIMPLE_NPC);
    }
}
