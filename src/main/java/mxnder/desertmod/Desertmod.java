package mxnder.desertmod;

import mxnder.desertmod.entity.ExampleNpcEntity;
import mxnder.desertmod.entity.SimpleNpcEntity;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Основной класс серверной части мода Desertmod.
 * <p>
 * Реализует интерфейс {@link ModInitializer} для инициализации мода на стороне сервера.
 * В данном моде серверная часть минимальна и включает только:
 * <ul>
 *   <li>Регистрацию сущностей (NPC) в реестре игры</li>
 *   <li>Регистрацию атрибутов сущностей (здоровье, скорость и т.д.)</li>
 * </ul>
 * </p>
 * <p>
 * Основная логика мода находится в клиентской части ({@link DesertmodClient}),
 * так как мод является client-only.
 * </p>
 *
 * @see ModInitializer интерфейс инициализации мода Fabric
 * @see DesertmodClient клиентская часть мода
 */
public class Desertmod implements ModInitializer {

    /**
     * Уникальный идентификатор мода.
     * Используется для регистрации ресурсов, сущностей и других объектов.
     */
    public static final String MOD_ID = "desertmod";

    /**
     * Логгер мода для вывода отладочной информации в консоль.
     * Имя логгера совпадает с ID мода для удобной фильтрации логов.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello, it's desertmod!");

        // Регистрация сущностей в реестре
        // Необходимо даже для client-only мода, так как рендер требует зарегистрированный тип сущности
        ModEntities.registerEntities();

        // Регистрация атрибутов сущностей
        // Определяет базовые характеристики NPC (здоровье, скорость передвижения)
        FabricDefaultAttributeRegistry.register(ModEntities.EXAMPLE_NPC, ExampleNpcEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.SIMPLE_NPC, SimpleNpcEntity.createAttributes());
    }
}