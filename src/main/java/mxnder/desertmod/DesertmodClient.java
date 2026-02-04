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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

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
        refisterKeybindings(); // регистр кнопки F
        registerNpcSpawning(); // обработчик нпс
        registerClientTicks(); // обработчик кнопки F
        registerNpcInteraction(); // обработчик пкс по нпс

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

    private void refisterKeybindings() {
        // РЕГИСТРАЦИЯ КЛАВИШИ F
        interactWithNpcKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.desertmod.interact", // ключ локализации названия
                        InputUtil.Type.KEYSYM, // тип ввода (клавиатура)
                        GLFW.GLFW_KEY_F, // клавиша F
                        KeyBinding.Category.GAMEPLAY) // существующая категория из игры
        );
    }

    private void registerNpcSpawning() {
        // РЕГИСТРАЦИЯ ОБРАБОТЧИКА СПАВНА NPC
        ClientTickEvents.END_CLIENT_TICK.register(ClientNpcSpawner::OnClientTick);
    }

    private void registerClientTicks() {
        // Общий клиентский тик: кнопки и визуальные таймеры
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (interactWithNpcKey.wasPressed()) {
                client.player.sendMessage(Text.literal("ты тыкнул кнопку взаимодействия"), false);
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

            if (!entity.getScoreboardTags().contains(ClientNpcSpawner.CAN_TALK_TAG)) {
                return ActionResult.PASS;
            }

            if (entity instanceof mxnder.desertmod.entity.SimpleNpcEntity) {
                if (!SIMPLE_NPC_DIALOG.canTalk(entity)) {
                    return ActionResult.PASS;
                }
                String phrase = SIMPLE_NPC_DIALOG.getNextPhrase();
                SIMPLE_NPC_DIALOG.showPhrase(entity, phrase, 60);
                return ActionResult.PASS;
            }

            if (entity instanceof mxnder.desertmod.entity.ExampleNpcEntity) {
                if (!EXAMPLE_NPC_DIALOG.canTalk(entity)) {
                    return ActionResult.PASS;
                }
                String phrase = EXAMPLE_NPC_DIALOG.getNextPhrase();
                EXAMPLE_NPC_DIALOG.showPhrase(entity, phrase, 60);
                return ActionResult.PASS;
            }

            return ActionResult.PASS;
        }));
    }
}