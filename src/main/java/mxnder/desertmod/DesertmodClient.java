package mxnder.desertmod;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import mxnder.desertmod.npc.ClientNpcSpawner;
import mxnder.desertmod.npc.ExampleNpcDialog;
import mxnder.desertmod.npc.SimpleNpcDialog;
import mxnder.desertmod.renderer.ExampleNpcRenderer;
import mxnder.desertmod.renderer.SimpleNpcRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

/**
 * Основной класс клиентского мода Desertmod.
 * <p>
 * Отвечает за инициализацию клиентской части мода, включая:
 * <ul>
 *   <li>Регистрацию рендеров для сущностей (NPC)</li>
 *   <li>Регистрацию клавиш управления (кнопка F для взаимодействия)</li>
 *   <li>Обработчики спавна NPC в мире</li>
 *   <li>Клиентские тики для обновления логики (таймеры фраз, обработка нажатий)</li>
 *   <li>Взаимодействие игрока с NPC (клик ПКМ по сущности)</li>
 *   <li>Загрузку конфигурации через YACL (YetAnotherConfigLib)</li>
 * </ul>
 * </p>
 * <p>
 * Мод является client-only, то есть работает только на стороне клиента
 * и не требует установки на сервере.
 * </p>
 *
 * @see ClientModInitializer интерфейс инициализации клиентского мода Fabric
 */
@Environment(EnvType.CLIENT)
public class DesertmodClient implements ClientModInitializer {

    /**
     * Клавиша для взаимодействия с NPC (по умолчанию F).
     * Используется для открытия меню конфигурации.
     */
    private static KeyBinding interactWithNpcKey;

    /**
     * Менеджер диалогов для простого NPC (житель пустыни).
     * Вынесен в отдельное поле для удобного доступа из разных методов.
     */
    private static final SimpleNpcDialog SIMPLE_NPC_DIALOG = new SimpleNpcDialog();

    /**
     * Менеджер диалогов для NPC-лесоруба (Example NPC).
     * Вынесен в отдельное поле для удобного доступа из разных методов.
     */
    private static final ExampleNpcDialog EXAMPLE_NPC_DIALOG = new ExampleNpcDialog();

    @Override
    public void onInitializeClient() {
        // Регистрация всех компонентов клиентского мода
        registerEntityRenderers();  // Регистрируем рендеры для отрисовки NPC
        registerKeybindings();      // Регистрируем клавишу взаимодействия (F)
        registerNpcSpawning();      // Регистрируем обработчик спавна NPC при загрузке мира
        registerClientTicks();      // Регистрируем клиентский тик для обработки нажатий и таймеров
        registerNpcInteraction();   // Регистрируем обработчик клика по NPC
        MyConfig.HANDLER.load();    // Загружаем конфигурацию из файла
    }

    /**
     * Регистрирует рендеры для всех типов сущностей мода.
     * <p>
     * Каждый тип сущности должен иметь свой рендер для корректной отрисовки в мире.
     * Рендеры используют GeckoLib для анимации моделей.
     * </p>
     */
    private void registerEntityRenderers() {
        EntityRendererRegistry.register(
                ModEntities.EXAMPLE_NPC,
                ctx -> new ExampleNpcRenderer<>(ctx, ModEntities.EXAMPLE_NPC)
        );

        EntityRendererRegistry.register(
                ModEntities.SIMPLE_NPC,
                ctx -> new SimpleNpcRenderer<>(ctx, ModEntities.SIMPLE_NPC)
        );
    }

    /**
     * Регистрирует клавишу взаимодействия с NPC.
     * <p>
     * По умолчанию используется клавиша F из категории GAMEPLAY.
     * При нажатии открывает меню конфигурации мода.
     * </p>
     */
    private void registerKeybindings() {
        interactWithNpcKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.desertmod.interact",    // Ключ локализации названия клавиши
                        InputUtil.Type.KEYSYM,       // Тип ввода: клавиатура
                        GLFW.GLFW_KEY_F,             // Код клавиши F
                        KeyBinding.Category.GAMEPLAY // Категория в настройках управления
                )
        );
    }

    /**
     * Регистрирует обработчик для спавна NPC при загрузке клиентского мира.
     * <p>
     * NPC появляются один раз за сессию в заранее определённых координатах.
     * </p>
     *
     * @see ClientNpcSpawner#OnClientTick(MinecraftClient)
     */
    private void registerNpcSpawning() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientNpcSpawner::OnClientTick);
    }

    /**
     * Регистрирует обработчик клиентских тиков.
     * <p>
     * Выполняется каждый тик (20 раз в секунду) и обрабатывает:
     * <ul>
     *   <li>Нажатие клавиши взаимодействия (открытие конфига)</li>
     *   <li>Обновление таймеров фраз для всех NPC</li>
     * </ul>
     * </p>
     */
    private void registerClientTicks() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Обработка нажатия клавиши взаимодействия
            if (interactWithNpcKey.wasPressed()) {
                client.player.sendMessage(Text.literal("Ты нажал кнопку взаимодействия"), false);

                // Создаём и открываем экран конфигурации YACL
                var yacl = new MyConfig().createYACL();
                client.setScreen(yacl.generateScreen(client.currentScreen));
            }

            // Обновляем таймеры фраз для обоих типов NPC
            SIMPLE_NPC_DIALOG.tick();
            EXAMPLE_NPC_DIALOG.tick();
        });
    }

    /**
     * Регистрирует обработчик взаимодействия с сущностями (клик ПКМ).
     * <p>
     * При клике по NPC отображает случайную фразу над его головой на 60 тиков (3 секунды).
     * Работает только на клиенте и только с основной рукой.
     * </p>
     */
    private void registerNpcInteraction() {
        UseEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            // Работаем только в клиентском мире
            if (!world.isClient()) return ActionResult.PASS;

            // Обрабатываем только основную руку (чтобы не дублировать при клике обеими руками)
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            // Обработка клика по простому NPC (житель пустыни)
            if (entity instanceof mxnder.desertmod.entity.SimpleNpcEntity) {
                String phrase = SIMPLE_NPC_DIALOG.getNextPhrase();
                SIMPLE_NPC_DIALOG.showPhrase(entity, phrase, 60);
                return ActionResult.PASS;
            }

            // Обработка клика по NPC-лесорубу
            if (entity instanceof mxnder.desertmod.entity.ExampleNpcEntity) {
                String phrase = EXAMPLE_NPC_DIALOG.getNextPhrase();
                EXAMPLE_NPC_DIALOG.showPhrase(entity, phrase, 60);
                return ActionResult.PASS;
            }

            return ActionResult.PASS;
        }));
    }
}