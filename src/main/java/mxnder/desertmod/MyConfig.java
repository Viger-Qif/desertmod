package mxnder.desertmod;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;

/**
 * Класс конфигурации мода Desertmod.
 * <p>
 * Предоставляет настройку параметров мода через библиотеку YACL (YetAnotherConfigLib).
 * Конфигурация сохраняется в файле {@code config/desertmod.json5} в формате JSON5.
 * </p>
 * <p>
 * Текущие настройки:
 * <ul>
 *   <li>enableNPC - включение/выключение спавна NPC</li>
 * </ul>
 * </p>
 *
 * @see ConfigClassHandler обработчик конфигурации YACL v2
 * @see YetAnotherConfigLib библиотека для создания экранов настроек
 */
public class MyConfig {

    /**
     * Обработчик конфигурации, управляющий загрузкой и сохранением настроек.
     * Использует сериализацию Gson с поддержкой формата JSON5.
     */
    public static ConfigClassHandler<MyConfig> HANDLER = ConfigClassHandler.createBuilder(MyConfig.class)
            .id(Identifier.of("desertmod", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("desertmod.json5"))
                    .setJson5(true)
                    .build())
            .build();

    /**
     * Настройка включения/выключения спавна NPC.
     * <p>
     * По умолчанию NPC включены ({@code true}). При значении {@code false}
     * NPC не будут появляться при загрузке мира.
     * </p>
     */
    @SerialEntry
    public boolean enableNPC = true;

    /**
     * Создаёт и настраивает экран конфигурации YACL.
     * <p>
     * Экран содержит:
     * <ul>
     *   <li>Заголовок "🏜️ Desert Mod"</li>
     *   <li>Вкладку "General" с настройками</li>
     *   <li>Опцию включения NPC с описанием</li>
     * </ul>
     * </p>
     *
     * @return настроенный экземпляр YACL для отображения экрана настроек
     */
    public YetAnotherConfigLib createYACL() {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("\uD83C\uDFDC\uFE0F Desert Mod")) // Заголовок окна (с эмодзи пустыни)
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General")) // Название вкладки
                        .tooltip(Text.literal("Основные настройки мода"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Включить NPC"))
                                .description(OptionDescription.of(Text.literal("Разрешает спавн NPC при загрузке мира")))
                                .binding(true,  // Значение по умолчанию
                                        () -> HANDLER.instance().enableNPC,  // Получение текущего значения
                                        newVal -> HANDLER.instance().enableNPC = newVal)  // Установка нового значения
                                .controller(TickBoxControllerBuilder::create)  // Тип контроллера: галочка (tick box)
                                .build())
                        .build())
                .save(() -> HANDLER.save()) // Действие при нажатии кнопки "Apply": сохранение в файл
                .build();
    }
}
