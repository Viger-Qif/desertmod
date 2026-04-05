package mxnder.desertmod.npc;

import java.util.Arrays;
import java.util.List;

// Диалоги примера NPC (список фраз)
public final class ExampleNpcDialog extends BaseNpcDialog {

    // Фразы для случайного выбора (изменить текст)
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

    public ExampleNpcDialog() {
        super(EXAMPLE_NPC_PHRASES);
    }
}
