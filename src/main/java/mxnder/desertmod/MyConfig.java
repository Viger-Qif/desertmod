package mxnder.desertmod;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import mxnder.desertmod.npc.ClientNpcSpawner;
import mxnder.desertmod.npc.NpcDataManager;
import mxnder.desertmod.screen.NpcEditorScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;

//  ОКНО НАСТРОЙКИ МОДА (ЗАВИСИМОСТЬ С БИБЛИОТЕКОЙ YACL)

public class MyConfig {


    // === ОБРАБОТЧИК КОНФИГА ===
    public static ConfigClassHandler<MyConfig> HANDLER = ConfigClassHandler.createBuilder(MyConfig.class)
            .id(Identifier.of("desertmod", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("desertmod.json5"))
                    .setJson5(true)
                    .build())
            .build();

    // === ПОЛЯ НАСТРОЕК ===

    // Группа 1: Настройки
    @SerialEntry
    public boolean enableNPC = true; // Вкл/выкл всех НПС
    @SerialEntry
    public boolean enableFrameInteraction = true; // Вкл/выкл взаимодействие с рамками

    // Группа 2: Редактор
    @SerialEntry
    public int npcRenderRadius = 50;

    // === ПОСТРОЕНИЕ ИНТЕРФЕЙСА ===
    public YetAnotherConfigLib createYACL() {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("\uD83C\uDFDC\uFE0F Desert Mod")) // Заголовок окна

                // === ЕДИНСТВЕННАЯ ВКЛАДКА: "Общее" ===
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Общее"))
                        .tooltip(Text.literal("Основные настройки мода"))

                        // ─── Группа 1: Настройки ───
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Настройки"))
                                .collapsed(false)

                                // 1. Включить/выключить всех НПС
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Отображение НПС"))
                                        .description(OptionDescription.of(Text.literal("Если выключено, НПС не будут спавниться и отображаться")))
                                        .binding(true,
                                                () -> HANDLER.instance().enableNPC,
                                                newVal -> {
                                                    HANDLER.instance().enableNPC = newVal;
                                                    ClientNpcSpawner.setNpcsEnabled(newVal);
                                                })
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                // 2. Взаимодействие с рамками
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Взаимодействие с рамками"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Разрешить игроку взаимодействовать с кастомными моделями в рамках\n" +
                                                        "\u00A77(Переименованными предметы)"
                                        )))
                                        .binding(true,
                                                () -> HANDLER.instance().enableFrameInteraction,
                                                newVal -> {
                                                    HANDLER.instance().enableFrameInteraction = newVal;
                                                })
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .build())

                        // ─── Группа 2: Редактор НПС ───
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Редактор НПС"))
                                .collapsed(false)

                                // 1. Кнопка: Открыть редактор
                                .option(ButtonOption.createBuilder()
                                        .name(Text.literal("\uD83D\uDD27 Открыть редактор"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Откроет отдельное окно для создания и расстановки НПС.\n" +
                                                        "\u00A77Требует загруженный мир"
                                        )))
                                        .text(Text.literal(""))
                                        .action((screen, opt) -> {
                                            openNpcEditor();
                                        })
                                        .build())

                                // 2. Статистика: Сколько всего / активных
                                .option(LabelOption.create(
                                        Text.literal("Всего: §a" + getTotalNpcCount() + " §7| Активные: §e" + getActiveNpcCount())
                                ))

                                // 3. Радиус прогрузки НПС
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("\uD83D\uDCCF Радиус прогрузки"))
                                        .description(OptionDescription.of(Text.literal(
                                                "НПС будут прогружаться только в этом радиусе от игрока\n" +
                                                        "\u00A77Меньше = лучше производительность"
                                        )))
                                        .binding(48,
                                                () -> HANDLER.instance().npcRenderRadius,
                                                newVal -> HANDLER.instance().npcRenderRadius = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(16, 128)      // Мин/макс
                                                .step(8)             // Шаг
                                                .formatValue(val -> Text.literal(val + " блоков")))
                                        .build())

                                .build())

                        .build())

                .save(() -> HANDLER.save())
                .build();
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private int cachedTotalCount = 0;
    private int cachedActiveCount = 0;
    private long lastStatsUpdate = 0;

    // Возвращает общее количество НПС в конфиге.
    private int getTotalNpcCount() {
        // Обновляем кэш не чаще 1 раза в 2 секунды
        if (System.currentTimeMillis() - lastStatsUpdate > 2000) {
            updateStatsCache();
        }
        return cachedTotalCount;
    }

    // Возвращает количество активных (включённых) НПС.
    private int getActiveNpcCount() {
        if (System.currentTimeMillis() - lastStatsUpdate > 2000) {
            updateStatsCache();
        }
        return cachedActiveCount;
    }

    // Отдельный метод для обновления кэша
    private void updateStatsCache() {
        try {
            var npcs = NpcDataManager.loadNpcs();
            cachedTotalCount = npcs != null ? npcs.size() : 0;
            cachedActiveCount = cachedTotalCount; // Пока все считаем активными
            lastStatsUpdate = System.currentTimeMillis();
        } catch (Exception e) {
            cachedTotalCount = 0;
            cachedActiveCount = 0;
        }
    }

    // Открывает кастомный экран редактора.
    private void openNpcEditor() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.player == null) {
            if (client.player != null) {
                client.player.sendMessage(
                        Text.literal("\u00A7c[DesertMod] Сначала загрузите мир!"),
                        false
                );
            }
            return;
        }

        Screen currentScreen = client.currentScreen;
        client.setScreen(new NpcEditorScreen(currentScreen));

    }
}
