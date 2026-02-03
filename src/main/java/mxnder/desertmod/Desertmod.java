package mxnder.desertmod;

import mxnder.desertmod.entity.ExampleNpcEntity;
import mxnder.desertmod.entity.SimpleNpcEntity;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Desertmod implements ModInitializer {
	public static final String MOD_ID = "desertmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("Hello, it`s desertmod!");
		ModEntities.registerEntities();
		FabricDefaultAttributeRegistry.register(ModEntities.EXAMPLE_NPC, ExampleNpcEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.SIMPLE_NPC, SimpleNpcEntity.createAttributes());
	}
}