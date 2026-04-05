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

public class MyConfig {


    // ОБРАБОТЧИК
    public static ConfigClassHandler<MyConfig> HANDLER = ConfigClassHandler.createBuilder(MyConfig.class)
            .id(Identifier.of("desertmod", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("desertmod.json5"))
                    .setJson5(true)
                    .build())
            .build();

    // Поле настройки (обязательно все переменные public)
    @SerialEntry
    public boolean enableNPC = true;

    public YetAnotherConfigLib createYACL() {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("\uD83C\uDFDC\uFE0F Desert Mod")) // Заголовок окна
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General")) // Название вкладки
                        .tooltip(Text.literal("Подсказка гайс"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Пример названия"))
                                .description(OptionDescription.of(Text.literal("Пример подсказки")))
                                .binding(true,
                                        () -> HANDLER.instance().enableNPC,
                                        newVal -> HANDLER.instance().enableNPC = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .save(() -> HANDLER.save()) // Что делать при нажатии "Apply": сохраняем файл
                .build();
    }
}
