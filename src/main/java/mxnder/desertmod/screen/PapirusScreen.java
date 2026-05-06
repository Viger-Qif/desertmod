package mxnder.desertmod.screen;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Экран для отображения изображения папируса.
 * Открывается при клике ПКМ по рамке с предметом "papirus".
 * Закрывается при нажатии любой клавиши.
 */

public class PapirusScreen extends Screen {

    // Укажите путь к вашей текстуре: assets/ваш мод/textures/gui/papirus_image.png
    private static final Identifier TEXTURE_ID = Identifier.of("desertmod", "textures/gui/papirus.png");

    public PapirusScreen() {
        super(Text.literal("Papirus View"));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true; // Закрывается по Esc
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Рисуем полупрозрачный темный фон (как при открытии сундука/инвентаря)
        // 0xAA000000 -> AA это прозрачность (чем больше, тем темнее), 000000 - черный цвет
        context.fill(0, 0, this.width, this.height, 0xAA000000);

        // Размеры вашей текстуры (укажите реальные размеры вашего файла png)
        int textureWidth = 16;
        int textureHeight = 16;

        // Вычисляем размер отображения (например, половина экрана или фиксированный размер)
        int drawWidth = Math.min(textureWidth, this.width / 2);
        int drawHeight = Math.min(textureHeight, this.height / 2);

        // Центрируем картинку
        int x = (this.width - drawWidth) / 2;
        int y = (this.height - drawHeight) / 2;


        // 3. Рисуем текстуру с использованием пайплайна
        // Аргументы: pipeline, sprite, x, y, u, v, width, height, texWidth, texHeight
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,           // Ваш пайплайн (стандартный для текстур)
                TEXTURE_ID,         // Идентификатор вашей картинки
                x,                  // Позиция X
                y,                  // Позиция Y
                0.0f,               // U (начало текстуры)
                0.0f,               // V (начало текстуры)
                drawWidth,          // Ширина отрисовки
                drawHeight,         // Высота отрисовки
                textureWidth,       // Полная ширина текстуры
                textureHeight       // Полная высота текстуры
        );

        super.render(context, mouseX, mouseY, delta);
    }
}
