package mxnder.desertmod;

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
import mxnder.desertmod.npc.NpcDataManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

// ОСНОВНОЙ ФАЙЛ ДЛЯ РАБОТЫ С КЛИЕНТОМ

@Environment(EnvType.CLIENT)
public class DesertmodClient implements ClientModInitializer {

    // Кнопка F (для взаимодействия)
    private static KeyBinding interactWithNpcKey;

    // Логика фраз вынесена в отдельный класс, чтобы она не мешалась в клиентской инициализации
    private static final SimpleNpcDialog SIMPLE_NPC_DIALOG = new SimpleNpcDialog();
    private static final ExampleNpcDialog EXAMPLE_NPC_DIALOG = new ExampleNpcDialog();

    @Override
    public void onInitializeClient() {

        registerEntityRenderers(); // регистр рендеров
        registerKeybindings(); // регистр кнопки X
        registerNpcSpawning(); // обработчик нпс
        registerClientTicks(); // обработчик кнопки X
        registerNpcInteraction(); // обработчик пкм по нпс
        registerItemFrameInteraction(); // обработчик пкм по рамке с предметом
        MyConfig.HANDLER.load(); // загрузка конфига (окна настроек)
        ItemFrameInteractionHandler.register();

        ClientNpcSpawner.syncFromConfig();
        // Запускаем FileWatcher для отслеживания изменений в JSON файле NPC
        NpcDataManager.startFileWatcher();

    }

    private void registerEntityRenderers() {
        // РЕГИСТРАЦИЯ РЕНДЕРОВ
        // ОБЯЗАТЕЛЬНО ДЛЯ КАЖДОГО ЭНТИТИ ОТДЕЛЬНО
        EntityRendererRegistry.register(
                ModEntities.EXAMPLE_NPC,
                ctx -> new ExampleNpcRenderer<>(ctx, ModEntities.EXAMPLE_NPC)
        );

        EntityRendererRegistry.register(
                ModEntities.SIMPLE_NPC,
                ctx -> new SimpleNpcRenderer<>(ctx, ModEntities.SIMPLE_NPC)
        );
    }

    private void registerKeybindings() {
        // РЕГИСТРАЦИЯ КЛАВИШИ F
        interactWithNpcKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.desertmod.interact", // ключ локализации названия
                        InputUtil.Type.KEYSYM, // тип ввода (клавиатура)
                        GLFW.GLFW_KEY_X, // клавиша X
                        KeyBinding.Category.GAMEPLAY) // существующая категория из игры
        );
    }

    private void registerNpcSpawning() {
        // РЕГИСТРАЦИЯ ОБРАБОТЧИКА СПАВНА NPC
        ClientTickEvents.END_CLIENT_TICK.register(ClientNpcSpawner::OnClientTick);
        // Регистрируем обработчик для обновления NPC при изменении радиуса рендера в реальном времени
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                ClientNpcSpawner.updateNpcVisibilityByRadius(client);
            }
        });
    }

    private void registerClientTicks() {
        // Общий клиентский тик: кнопки и визуальные таймеры
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (interactWithNpcKey.wasPressed()) {
                var yacl = new MyConfig().createYACL();
                client.setScreen(yacl.generateScreen(client.currentScreen));
            }

            SIMPLE_NPC_DIALOG.tick();
            EXAMPLE_NPC_DIALOG.tick();
        });
    }

    private void registerNpcInteraction() {
        UseEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            // client-only мод
            if (!world.isClient()) return ActionResult.PASS;

            // работаем только с основной рукой
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;


            if (entity instanceof mxnder.desertmod.entity.SimpleNpcEntity) {
                String phrase = SIMPLE_NPC_DIALOG.getNextPhrase();
                SIMPLE_NPC_DIALOG.showPhrase(entity, phrase, 60);
                return ActionResult.PASS;
            }

            if (entity instanceof mxnder.desertmod.entity.ExampleNpcEntity) {
                String phrase = EXAMPLE_NPC_DIALOG.getNextPhrase();
                EXAMPLE_NPC_DIALOG.showPhrase(entity, phrase, 60);
                return ActionResult.PASS;
            }

            return ActionResult.PASS;
        }));
    }

    private void registerItemFrameInteraction() {
        ItemFrameInteractionHandler.register();
    }
}