package mxnder.desertmod.npc;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Управляет фразами для простых NPC:
 * - хранит список реплик
 * - раздаёт их по очереди в случайном порядке
 * - показывает реплику над головой NPC на ограниченное время
 */
public final class SimpleNpcDialog {

    // Базовый список реплик. Он неизменяемый и используется как источник.
    private static final List<String> SIMPLE_NPC_PHRASES = Arrays.asList(
            "Пески шепчут что-то неладное",
            "Фараон строит новую пирамиду",
            "Редко здесь бывают чужаки",
            "Бывал ли ты в Сиве?",
            "Нужно помолиться в храме",
            "Да простит меня Амон-Ра",
            "Не поверишь, но я видел жемчуг",
            "Засуха опустилась на наши земли",
            "Иду молиться Исиде за семью",
            "У меня родился сын - Саамон",
            "Жрецы обещают разлив Нила",
            "Нил всё ещё не у красной отметки",
            "Как бы хватило еды на этот год",
            "Амон-Ра видит все наши деяния",
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
            "Что ищешь здесь, незнакомец?",
            "День выдался тяжёлым",
            "Надо успеть все дла до заката",
            "Пески не любят беспечных"
    );

    // Перемешанный список (хранит текущий порядок фраз)
    private final List<String> shuffledSimplePhrases = new ArrayList<>(SIMPLE_NPC_PHRASES);
    // Индекс текущей фразы (какую фразу сейчас показать)
    private int simpleNpcIndex = 0;

    // Таймеры показа реплики над NPC
    private final Map<Entity, Integer> npcPhraseTimers = new HashMap<>();
    // КД МЕЖДУ ФРАЗАМИ
    private static final Map<Entity, Integer> npcTalkCooldowns = new HashMap<>();
    private static final int NPC_TALK_COOLDOWN_TICKS = 40;

    /**
     * Возвращает следующую фразу по очереди.
     * Когда очередь заканчивается, список перемешивается заново.
     */
    public String getNextPhrase() {
        if (simpleNpcIndex == 0) {
            Collections.shuffle(shuffledSimplePhrases);
        }

        String phrase = shuffledSimplePhrases.get(simpleNpcIndex);
        simpleNpcIndex++;

        if (simpleNpcIndex >= shuffledSimplePhrases.size()) {
            simpleNpcIndex = 0;
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
        npcTalkCooldowns.put(npc, NPC_TALK_COOLDOWN_TICKS);
    }

    public boolean canTalk(Entity npc) {
        return npcTalkCooldowns.getOrDefault(npc, 0) <= 0;
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

        Iterator<Map.Entry<Entity, Integer>> cooldownIterator = npcTalkCooldowns.entrySet().iterator();

        while (cooldownIterator.hasNext()) {
            Map.Entry<Entity, Integer> entry = cooldownIterator.next();
            Entity npc = entry.getKey();
            int timeLeft = entry.getValue() - 1;

            if (timeLeft <= 0 || npc.isRemoved()) {
                cooldownIterator.remove();
            } else {
                entry.setValue(timeLeft);
            }
        }
    }
}
