package mxnder.desertmod.npc;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.*;

// Базовый класс диалогов NPC (управление фразами и таймерами)
public abstract class BaseNpcDialog {

    private final List<String> basePhrases;
    private final List<String> shuffledPhrases;
    private int currentIndex = 0;
    private final Map<Entity, Integer> npcPhraseTimers = new HashMap<>();

    // Инициализация списка фраз
    protected BaseNpcDialog(List<String> phrases) {
        this.basePhrases = Collections.unmodifiableList(new ArrayList<>(phrases));
        this.shuffledPhrases = new ArrayList<>(basePhrases);
    }

    // Получить следующую фразу (перемешивает при достижении конца)
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

    // Показать фразу над NPC на указанное время (ticks)
    public void showPhrase(Entity npc, String phrase, int ticks) {
        npc.setCustomName(Text.literal(phrase));
        npc.setCustomNameVisible(true);
        npcPhraseTimers.put(npc, ticks);
    }

    // Обновление таймеров фраз (каждый тик)
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

    // Количество фраз
    public int getPhraseCount() {
        return basePhrases.size();
    }

    // Есть ли фразы
    public boolean hasPhrases() {
        return !basePhrases.isEmpty();
    }
}
