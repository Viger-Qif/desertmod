package mxnder.desertmod.npc;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.*;

/**
 * Базовый класс для управления диалогами NPC.
 * <p>
 * Предоставляет функциональность для:
 * <ul>
 *   <li>Хранения списка фраз (реплик) для NPC</li>
 *   <li>Последовательной выдачи фраз в случайном порядке</li>
 *   <li>Отображения фраз над головой NPC на ограниченное время</li>
 *   <li>Автоматического скрытия фраз по истечении времени</li>
 * </ul>
 * </p>
 * <p>
 * Этот класс является абстрактным и должен быть расширен конкретными классами
 * для каждого типа NPC с собственным набором фраз.
 * </p>
 *
 * @see SimpleNpcDialog
 * @see ExampleNpcDialog
 */
public abstract class BaseNpcDialog {

    /**
     * Базовый список реплик. Неизменяемый, используется как источник для перемешивания.
     */
    private final List<String> basePhrases;

    /**
     * Перемешанный список фраз (хранит текущий порядок выдачи).
     */
    private final List<String> shuffledPhrases;

    /**
     * Индекс текущей фразы в перемешанном списке.
     */
    private int currentIndex = 0;

    /**
     * Таймеры отображения фраз для каждого NPC.
     * Ключ - сущность NPC, значение - оставшееся время в тиках.
     */
    private final Map<Entity, Integer> npcPhraseTimers = new HashMap<>();

    /**
     * Конструктор базового класса диалога.
     *
     * @param phrases список фраз для данного типа NPC
     */
    protected BaseNpcDialog(List<String> phrases) {
        this.basePhrases = Collections.unmodifiableList(new ArrayList<>(phrases));
        this.shuffledPhrases = new ArrayList<>(basePhrases);
    }

    /**
     * Возвращает следующую фразу по очереди.
     * <p>
     * Когда все фразы были использованы, список автоматически перемешивается заново.
     * </p>
     *
     * @return следующая фраза из списка
     */
    public String getNextPhrase() {
        if (currentIndex == 0) {
            Collections.shuffle(shuffledPhrases);
        }

        String phrase = shuffledPhrases.get(currentIndex);
        currentIndex++;

        if (currentIndex >= shuffledPhrases.size()) {
            currentIndex = 0;
        }

        return phrase;
    }

    /**
     * Отображает фразу над головой указанного NPC на заданное количество тиков.
     *
     * @param npc   сущность NPC, над которой будет показана фраза
     * @param phrase текст фразы для отображения
     * @param ticks  время отображения в тиках (1 тик = 1/20 секунды)
     */
    public void showPhrase(Entity npc, String phrase, int ticks) {
        npc.setCustomName(Text.literal(phrase));
        npc.setCustomNameVisible(true);
        npcPhraseTimers.put(npc, ticks);
    }

    /**
     * Обновляет таймеры отображения фраз и скрывает фразы, когда время истекает.
     * <p>
     * Этот метод должен вызываться каждый тик для корректной работы таймеров.
     * Также удаляет записи для удалённых из мира NPC.
     * </p>
     */
    public void tick() {
        Iterator<Map.Entry<Entity, Integer>> iterator = npcPhraseTimers.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Entity, Integer> entry = iterator.next();
            Entity npc = entry.getKey();
            int timeLeft = entry.getValue() - 1;

            // Если время вышло или NPC удалён из мира
            if (timeLeft <= 0 || npc.isRemoved()) {
                npc.setCustomNameVisible(false);
                npc.setCustomName(null);
                iterator.remove();
            } else {
                entry.setValue(timeLeft);
            }
        }
    }

    /**
     * Возвращает количество доступных фраз для данного типа NPC.
     *
     * @return количество фраз в базовом списке
     */
    public int getPhraseCount() {
        return basePhrases.size();
    }

    /**
     * Проверяет, есть ли фразы для данного типа NPC.
     *
     * @return true если список фраз не пуст
     */
    public boolean hasPhrases() {
        return !basePhrases.isEmpty();
    }
}
