package mxnder.desertmod;

import mxnder.desertmod.entity.SimpleNpcEntity;
import mxnder.desertmod.npc.ClientNpcSpawner;
import mxnder.desertmod.renderer.ExampleNpcRenderer;
import mxnder.desertmod.renderer.SimpleNpcRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@Environment(EnvType.CLIENT)
public class DesertmodClient implements ClientModInitializer {

    // Кнопка F
    private static KeyBinding interactWithNpcKey;

    // Список фраз для SimpleNpc
    private static final List<String> SIMPLE_NPC_PHRASES = Arrays.asList(
        "Пески шепчут что-то неладное",
        "Фараон строит новую пирамиду",
        "Бывал ли ты в Сиве?",
        "Нужно помолиться в храме",
        "Да простит меня Амон-Ра",
        "Не поверишь, но я видел папирус",
        "Засуха опустилась на наши земли",
        "Иду молиться Исиде за семью",
        "У меня родился сын - Саамон",
        "Жрецы обещают разлив Нила",
        "Нил до сих пор не у красной отметки",
        "Как бы хватило еды на этот год",
        "Амон-Ра видит все наши мысли",
        "Что привело тебя сюда, странник?",
        "Не видел тебя здесь раньше",
        "Ты не видел моего осла?",
        "Сейчас даже песок обжигает",
        "Если ты привёз лён, иди на рынок",
        "Хапи разгневан - воды мало",
        "Мой прадед строил пирамиду",
        "Остерегайся песков после заката",
        "Солнце безжалостно ко всем",
        "Пески более жестоки, чем кажется",
        "Что ищешь здесь, незнакомец?"
    );

    // Перемешанный список (хранит текущий порядок фраз)
    private static List<String> shuffledSimplePhrases = new ArrayList<>(SIMPLE_NPC_PHRASES);
    // Индекс текущей фразы (какую фразу сейчас показать)
    private static int simpleNpcIndex = 0;

    // --- Для отслеживания ПКМ ---
    private static final Map<Entity, Integer> npcPhraseTimers = new HashMap<>();

    @Override
    public void onInitializeClient() {

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

        // РЕГИСТРАЦИЯ КЛАВИШИ F
        interactWithNpcKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.desertmod.interact", // ключ локализации названия
                        InputUtil.Type.KEYSYM, // тип ввода (клавиатура)
                        GLFW.GLFW_KEY_F, // клавиша F
                        KeyBinding.Category.GAMEPLAY) // существующая категория из игры
        );

        // РЕГИСТРАЦИЯ ОБРАБОТЧИКА СПАВНА NPC
        ClientTickEvents.END_CLIENT_TICK.register(ClientNpcSpawner::OnClientTick);

        // РЕГИСТРАЦИЯ ОБРАБОТЧИКА НАЖАТИЯ КЛАВИШИ F
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (interactWithNpcKey.wasPressed()) {
                client.player.sendMessage(Text.literal("ты тыкнул кнопку взаимодействия"), false);
            }

            Iterator<Map.Entry<Entity, Integer>> iterator = npcPhraseTimers.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Entity, Integer> entry = iterator.next();

                Entity npc = entry.getKey();
                int timeLeft = entry.getValue() - 1;

                if (timeLeft <= 0 || npc.isRemoved()) {
                    npc.setCustomNameVisible(false);
                    npc.setCustomName(null);
                    iterator.remove();
                } else {
                    entry.setValue(timeLeft);
                }
            }
        });

        UseEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            // client-only мод
            if (!world.isClient()) return ActionResult.PASS;

            // работаем только с основной рукой
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            // === ЭТО РОВНО ТО, ЧТО У ТЕБЯ БЫЛО НА F ===
            if (entity instanceof mxnder.desertmod.entity.SimpleNpcEntity) {

                String phrase = getNextPhrase();
                //player.sendMessage(Text.literal(phrase), false);

                entity.setCustomName(Text.literal(phrase));
                entity.setCustomNameVisible(true);

                showNpcPhraseForTicks(entity, 60);

                return ActionResult.PASS;
            }

            if (entity instanceof mxnder.desertmod.entity.ExampleNpcEntity) {
                //player.sendMessage(Text.literal("ку"), false);
                //int randomPhrase =
                return ActionResult.PASS;
            }

            return ActionResult.PASS;
        }));
    }

    private String getNextPhrase() {
        // Если это первая фраза (индекс 0), то перемешиваем список
        if (simpleNpcIndex == 0) {
            Collections.shuffle(shuffledSimplePhrases);
        }

        // Получаем фразу по текущему индексу
        String phrase = shuffledSimplePhrases.get(simpleNpcIndex);

        // Увеличиваем индекс для следующего раза
        simpleNpcIndex++;

        // Если индекс дошёл до конца списка, сбрасываем его на 0
        if (simpleNpcIndex >= shuffledSimplePhrases.size()) {
            simpleNpcIndex = 0;
        }

        return phrase;
    }

    private static void showNpcPhraseForTicks(Entity npc, int ticks) {
        npcPhraseTimers.put(npc, ticks);
    }
}