package mxnder.desertmod.npc;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.*;

/**
 * Управляет фразами для простых NPC:
 * - хранит список реплик
 * - раздаёт их по очереди в случайном порядке
 * - показывает реплику над головой NPC на ограниченное время
 */
public final class ExampleNpcDialog {

    // Базовый список реплик. Он неизменяемый и используется как источник.
    private static final List<String> EXAMPLE_NPC_PHRASES = Arrays.asList(
            "Не мешай, я занят",
            "Не отвлекай от работы",
            "Сейчас не до тебя",
            "Мне нужно работать",
            "Ты не вовремя, я работаю",
            "Не говори под руку",
            "Я слишком занят для разговоров",
            "Опять работа",
            "Приходи потом",
            "Руки заняты делом"
    );

    // Перемешанный список (хранит текущий порядок фраз)
    private final List<String> shuffledExamplePhrases = new ArrayList<>(EXAMPLE_NPC_PHRASES);
    // Индекс текущей фразы (какую фразу сейчас показать)
    private int exampleNpcIndex = 0;

    // Таймеры показа реплики над NPC
    private final Map<Entity, Integer> npcPhraseTimers = new HashMap<>();

    /**
     * Возвращает следующую фразу по очереди.
     * Когда очередь заканчивается, список перемешивается заново.
     */
    public String getNextPhrase() {
        if (exampleNpcIndex == 0) {
            Collections.shuffle(shuffledExamplePhrases);
        }

        String phrase = shuffledExamplePhrases.get(exampleNpcIndex);
        exampleNpcIndex++;

        if (exampleNpcIndex >= shuffledExamplePhrases.size()) {
            exampleNpcIndex = 0;
        }

        return phrase;
    }

    /**
     * Показывает фразу над NPC на указанное число тиков.
     */
    public void showPhrase(Entity npc, String phrase, int ticks) {
        npc.setCustomName(Text.literal(phrase));
        npc.setCustomNameVisible(true);
        npcPhraseTimers.put(npc, ticks);
    }

    /**
     * Обновляет таймеры и скрывает фразы, когда время истекает.
     * Этот метод нужно вызывать раз в тик.
     */
    public void tick() {
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
    }
}
