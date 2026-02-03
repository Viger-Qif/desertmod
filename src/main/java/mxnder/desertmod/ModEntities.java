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

public class ModEntities {

    public static final RegistryKey<EntityType<?>> NPC_KEY =
            RegistryKey.of(
                    RegistryKeys.ENTITY_TYPE,
                    Identifier.of(Desertmod.MOD_ID, "npc")
            );
    
    // ссылка на нпс
    public static final EntityType<ExampleNpcEntity> EXAMPLE_NPC = FabricEntityTypeBuilder
            .create(SpawnGroup.MISC, ExampleNpcEntity::new)
            .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
            .build(NPC_KEY);

    public static final EntityType<SimpleNpcEntity> SIMPLE_NPC = FabricEntityTypeBuilder
            .create(SpawnGroup.MISC, SimpleNpcEntity::new)
            .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
            .build(NPC_KEY);

    // метод регистрации сущностей
    public static void registerEntities() {
        Registry.register(Registries.ENTITY_TYPE, Identifier.of("desertmod", "npc_lamberjack"), EXAMPLE_NPC);
        Registry.register(Registries.ENTITY_TYPE, Identifier.of("desertmod", "npc_simple_desert"), SIMPLE_NPC);
    }

}
