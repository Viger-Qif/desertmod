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

// Клиентская инициализация мода (рендеры, спавн, взаимодействие)
@Environment(EnvType.CLIENT)
public class DesertmodClient implements ClientModInitializer {

    // Клавиша взаимодействия с NPC (по умолчанию F)
    private static KeyBinding interactWithNpcKey;

    // Диалоги для NPC
    private static final SimpleNpcDialog SIMPLE_NPC_DIALOG = new SimpleNpcDialog();
    private static final ExampleNpcDialog EXAMPLE_NPC_DIALOG = new ExampleNpcDialog();

    @Override
    public void onInitializeClient() {
        registerEntityRenderers();
        registerKeybindings();
        registerNpcSpawning();
        registerClientTicks();
        registerNpcInteraction();
        MyConfig.HANDLER.load();
    }

    // Регистрация рендереров для NPC
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

    // Регистрация клавиши взаимодействия
    private void registerKeybindings() {
        interactWithNpcKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.desertmod.interact",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_F,  // Изменить клавишу
                        KeyBinding.Category.GAMEPLAY
                )
        );
    }

    // Спавн NPC при заходе в мир
    private void registerNpcSpawning() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientNpcSpawner::OnClientTick);
    }

    // Тики клиента: обработка клавиш и диалогов
    private void registerClientTicks() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (interactWithNpcKey.wasPressed()) {
                client.player.sendMessage(Text.literal("Ты нажал кнопку взаимодействия"), false);

                var yacl = new MyConfig().createYACL();
                client.setScreen(yacl.generateScreen(client.currentScreen));
            }

            SIMPLE_NPC_DIALOG.tick();
            EXAMPLE_NPC_DIALOG.tick();
        });
    }

    // Взаимодействие с NPC (правый клик)
    private void registerNpcInteraction() {
        UseEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            if (!world.isClient()) return ActionResult.PASS;

            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            if (entity instanceof mxnder.desertmod.entity.SimpleNpcEntity) {
                String phrase = SIMPLE_NPC_DIALOG.getNextPhrase();
                SIMPLE_NPC_DIALOG.showPhrase(entity, phrase, 60);  // 60 ticks = 3 секунды
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
}
