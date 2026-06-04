package mxnder.desertmod.screen;

import mxnder.desertmod.npc.ClientNpcSpawner;
import mxnder.desertmod.npc.NpcDataManager;
import mxnder.desertmod.npc.NpcEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Экран редактора НПС — создание и управление НПС в мире.
public class NpcEditorScreen extends Screen {

    private final Screen parentScreen;

    // === Поля формы ===
    private TextFieldWidget idField;
    private ButtonWidget typeButton;
    private ButtonWidget animButton;

    private TextFieldWidget xField;
    private TextFieldWidget yField;
    private TextFieldWidget zField;
    private TextFieldWidget yawField;

    // === Кнопки ===
    private ButtonWidget copyPosButton;
    private ButtonWidget saveButton;
    private ButtonWidget clearButton;

    // === Фильтр списка ===
    private ButtonWidget filterRadiusButton;
    private ButtonWidget filterAllButton;
    private boolean showAllNpcs = false;

    // === Состояние и кэш ===
    private NpcEntry editingEntry = null;
    private List<NpcEntry> npcList;
    private long lastListRefresh = 0;
    private boolean isNewNpc = true;

    // ✅ НОВОЕ: список кнопок для списка НПС (чтобы потом очистить)
    private final List<ButtonWidget> listButtons = new ArrayList<>();

    // === Списки для переключателей ===
    private static final List<String> NPC_TYPES = List.of("simple", "example");
    private static final Map<String, List<String>> ANIMS_BY_TYPE = Map.of(
            "simple", List.of("idle_hat", "idle_hair", "lean_1_hair", "talk_hair"),
            "example", List.of("idle")
    );

    private String currentType = "simple";
    private String currentAnim = "idle";

    // === Позиции элементов ===
    private int formX = 50;
    private int formY = 30;
    private int fieldWidth = 100;
    private int fieldHeight = 20;
    private int gap = 22;

    private static final int MAX_DISPLAY_NPCS = 30;
    private static final int LIST_ITEM_HEIGHT = 12;
    private static final int LIST_VISIBLE_HEIGHT = 150;

    // ✅ ИСПРАВЛЕНО: тип double (было 0f — float)
    private double listScrollOffset = 0.0;

    public NpcEditorScreen(Screen parentScreen) {
        super(Text.literal("📝 Редактор НПС"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        refreshNpcList();
        clearChildren();

        clearListButtons();

        // === СТРОКА 1: ID + Тип + Анимация ===
        idField = createReadOnlyTextField(formX, formY, 120, fieldHeight,
                editingEntry != null ? editingEntry.id() : generateAutoId());
        addDrawableChild(idField);

        typeButton = ButtonWidget.builder(
                Text.literal("Тип: §e" + currentType),
                btn -> cycleType()
        ).position(formX + 130, formY).size(90, fieldHeight).build();
        addDrawableChild(typeButton);

        animButton = ButtonWidget.builder(
                Text.literal("Анимация: §e" + currentAnim),
                btn -> cycleAnim()
        ).position(formX + 230, formY).size(100, fieldHeight).build();
        addDrawableChild(animButton);

        // === СТРОКА 2: Координаты + 📍 ===
        xField = createNumberField(formX, formY + gap, 55, fieldHeight, "X",
                editingEntry != null ? String.format("%.1f", editingEntry.x()) : "");
        addDrawableChild(xField);

        yField = createNumberField(formX + 60, formY + gap, 55, fieldHeight, "Y",
                editingEntry != null ? String.format("%.1f", editingEntry.y()) : "");
        addDrawableChild(yField);

        zField = createNumberField(formX + 120, formY + gap, 55, fieldHeight, "Z",
                editingEntry != null ? String.format("%.1f", editingEntry.z()) : "");
        addDrawableChild(zField);

        yawField = createNumberField(formX + 180, formY + gap, 50, fieldHeight, "Yaw",
                editingEntry != null ? String.format("%.1f", editingEntry.yaw()) : "");
        addDrawableChild(yawField);

        copyPosButton = ButtonWidget.builder(
                Text.literal("Взять с меня"),
                btn -> copyPlayerPosAndYaw()
        ).position(formX + 235, formY + gap).size(110, fieldHeight).build();
        addDrawableChild(copyPosButton);

        // === СТРОКА 3: Кнопки действий ===
        saveButton = ButtonWidget.builder(
                Text.literal("Сохранить"),
                btn -> saveNpc()
        ).position(formX, formY + gap * 2 + 5).size(100, 20).build();
        addDrawableChild(saveButton);

        clearButton = ButtonWidget.builder(
                Text.literal("Сброс"),
                btn -> clearForm()
        ).position(formX + 110, formY + gap * 2 + 5).size(80, 20).build();
        addDrawableChild(clearButton);

        // === ФИЛЬТР СПИСКА ===
        int filterY = formY + gap * 3 + 15;

        filterRadiusButton = ButtonWidget.builder(
                Text.literal("● В радиусе"),
                btn -> setFilterMode(false)
        ).position(formX, filterY).size(80, 18).build();
        addDrawableChild(filterRadiusButton);

        filterAllButton = ButtonWidget.builder(
                Text.literal("○ Все"),
                btn -> setFilterMode(true)
        ).position(formX + 85, filterY).size(60, 18).build();
        addDrawableChild(filterAllButton);

        updateFilterButtonsVisual();
        // ✅ Создаём кнопки для списка НПС
        createListButtons();
    }

    private TextFieldWidget createReadOnlyTextField(int x, int y, int width, int height, String text) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, width, height, Text.literal("ID"));
        field.setText(text);
        field.setEditable(false);
        field.setMaxLength(32);
        return field;
    }

    private TextFieldWidget createNumberField(int x, int y, int width, int height, String hint, String value) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, width, height, Text.literal(hint));
        field.setText(value);
        field.setMaxLength(10);
        return field;
    }

    private void cycleType() {
        int idx = NPC_TYPES.indexOf(currentType);
        currentType = NPC_TYPES.get((idx + 1) % NPC_TYPES.size());

        List<String> anims = ANIMS_BY_TYPE.getOrDefault(currentType, List.of("idle"));
        currentAnim = anims.isEmpty() ? "idle" : anims.get(0);

        typeButton.setMessage(Text.literal("Тип: §e" + currentType));
        animButton.setMessage(Text.literal("Аним: §e" + currentAnim));

        if (isNewNpc) {
            idField.setText(generateAutoId());
        }
    }

    private void cycleAnim() {
        List<String> anims = ANIMS_BY_TYPE.getOrDefault(currentType, List.of("idle"));
        if (anims.isEmpty()) return;

        int idx = anims.indexOf(currentAnim);
        currentAnim = anims.get((idx + 1) % anims.size());
        animButton.setMessage(Text.literal("Аним: §e" + currentAnim));
    }

    private void setFilterMode(boolean showAll) {
        showAllNpcs = showAll;
        updateFilterButtonsVisual();

        clearListButtons();
        createListButtons();
    }

    private void updateFilterButtonsVisual() {
        if (!showAllNpcs) {
            filterRadiusButton.setMessage(Text.literal("● В радиусе"));
            filterAllButton.setMessage(Text.literal("○ Все"));
        } else {
            filterRadiusButton.setMessage(Text.literal("○ В радиусе"));
            filterAllButton.setMessage(Text.literal("● Все"));
        }
    }

    private void createListButtons() {
        if (npcList == null) {
            refreshNpcList();
            if (npcList == null) return;
        }

        var player = MinecraftClient.getInstance().player;
        int listY = formY + gap * 3 + 40;
        int baseY = listY + 12;
        int listBottom = baseY + LIST_VISIBLE_HEIGHT;

        int displayCount = 0;
        int firstVisibleIndex = (int) Math.ceil(listScrollOffset / LIST_ITEM_HEIGHT);

        for (int i = firstVisibleIndex; i < npcList.size(); i++) {
            NpcEntry npc = npcList.get(i);

            // ✅ Считаем позицию относительно скролла
            int npcY = baseY + (i * LIST_ITEM_HEIGHT) - (int) listScrollOffset;

            // Прерываем, если вышли за нижнюю границу
            if (npcY >= listBottom) {
                break;
            }

            // Пропускаем элементы выше видимой области
            if (npcY + LIST_ITEM_HEIGHT < baseY) {
                continue;
            }

            // Фильтр по радиусу
            if (!showAllNpcs && player != null) {
                Vec3d npcPos = new Vec3d(npc.x(), npc.y(), npc.z());
                Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                double dist = playerPos.distanceTo(npcPos);
                int radius = mxnder.desertmod.MyConfig.HANDLER.instance().npcRenderRadius;
                if (dist > radius) {
                    continue;
                }
            }


            // ✅ Лимит как в render()
            if (displayCount >= MAX_DISPLAY_NPCS) {
                break;
            }

            // Позиции кнопок
            int btnX = width - formX - 50; // ✅ Прижимаем к правому краю формы

            ButtonWidget editBtn = ButtonWidget.builder(
                            Text.literal("✎"),
                            btn -> editNpc(npc)
                    )
                    .position(btnX, npcY)
                    .size(20, LIST_ITEM_HEIGHT)
                    .build();
            addDrawableChild(editBtn);
            listButtons.add(editBtn);

            ButtonWidget delBtn = ButtonWidget.builder(
                            Text.literal("🗑"),
                            btn -> deleteNpc(npc.id())
                    )
                    .position(btnX + 25, npcY)
                    .size(20, LIST_ITEM_HEIGHT)
                    .build();
            addDrawableChild(delBtn);
            listButtons.add(delBtn);

            displayCount++;
        }
    }

    private void copyPlayerPosAndYaw() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        xField.setText(String.format("%.1f", player.getX()));
        yField.setText(String.format("%.1f", player.getY()));
        zField.setText(String.format("%.1f", player.getZ()));

        float yaw = player.getYaw();
        if (yaw < 0) yaw += 360;
        yawField.setText(String.format("%.1f", yaw));

        playClickSound();
    }

    private void refreshNpcList() {
        List<NpcEntry> loaded = NpcDataManager.loadNpcs();
        npcList = (loaded != null) ? loaded : new ArrayList<>();
        lastListRefresh = System.currentTimeMillis();
    }

    private String generateAutoId() {
        if (npcList == null) return "auto_01";

        int maxNum = 0;
        for (NpcEntry npc : npcList) {
            if (npc.id().matches(currentType + "_\\d+")) {
                try {
                    int num = Integer.parseInt(npc.id().substring(currentType.length() + 1));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%s_%02d", currentType, maxNum + 1);
    }

    private void saveNpc() {
        try {
            String id = idField.getText().trim();
            if (id.isEmpty()) {
                sendMessage("§c[Ошибка] ID не может быть пустым!");
                return;
            }

            double x = parseDouble(xField.getText(), 0);
            double y = parseDouble(yField.getText(), 64);
            double z = parseDouble(zField.getText(), 0);
            float yaw = parseFloat(yawField.getText(), 0);

            NpcEntry entry = new NpcEntry(id, currentType, x, y, z, yaw, currentAnim);

            NpcDataManager.saveNpc(entry);

            npcList = NpcDataManager.loadNpcs();
            lastListRefresh = System.currentTimeMillis();

            ClientNpcSpawner.refreshAllNpcs();

            sendMessage("§a[✔] НПС сохранён: " + id);
            clearForm();
            playClickSound();

            init();
        } catch (Exception e) {
            sendMessage("§c[Ошибка] " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        editingEntry = null;
        isNewNpc = true;

        idField.setText(generateAutoId());
        xField.setText("");
        yField.setText("");
        zField.setText("");
        yawField.setText("");
        currentType = "simple";
        currentAnim = "idle";
        typeButton.setMessage(Text.literal("Тип: §e" + currentType));
        animButton.setMessage(Text.literal("Аним: §e" + currentAnim));
        playClickSound();
    }

    public void editNpc(NpcEntry entry) {
        editingEntry = entry;
        isNewNpc = false;

        idField.setText(entry.id());
        xField.setText(String.format("%.1f", entry.x()));
        yField.setText(String.format("%.1f", entry.y()));
        zField.setText(String.format("%.1f", entry.z()));
        yawField.setText(String.format("%.1f", entry.yaw()));

        currentType = entry.typeKey();
        currentAnim = entry.animVariant();
        typeButton.setMessage(Text.literal("Тип: §e" + currentType));
        animButton.setMessage(Text.literal("Аним: §e" + currentAnim));

        playClickSound();
    }

    public void deleteNpc(String id) {
        NpcDataManager.deleteNpc(id);
        npcList = NpcDataManager.loadNpcs();
        lastListRefresh = System.currentTimeMillis();

        ClientNpcSpawner.refreshAllNpcs();

        sendMessage("§a[✔] НПС удалён: " + id);
        if (editingEntry != null && editingEntry.id().equals(id)) {
            clearForm();
        }
        playClickSound();
        init();
    }

    private void clearListButtons() {
        for (ButtonWidget btn : listButtons) {
            remove(btn);
        }
        listButtons.clear();
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            if (text.isEmpty()) return defaultValue;
            return Double.parseDouble(text.replace(',', '.'));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private float parseFloat(String text, float defaultValue) {
        try {
            if (text.isEmpty()) return defaultValue;
            return Float.parseFloat(text.replace(',', '.'));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void sendMessage(String message) {
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.sendMessage(Text.literal(message), false);
        }
    }

    private void playClickSound() {
        // Можно добавить звук позже
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Заголовок формы
        String formTitle = editingEntry != null ? "✏️ Редактирование" : "➕ Создание НПС";
        context.drawText(textRenderer, formTitle, formX, formY - 12, 0xFFFFFF, true);

        // Заголовок списка
        int listY = formY + gap * 3 + 40;
        context.drawText(textRenderer, "📋 Список НПС", formX, listY, 0xFFFFA0, true);

        // ✅ ПРОВЕРКА: гарантируем, что список не null
        if (npcList == null) {
            refreshNpcList();
        }

        // Рисуем список НПС
        if (npcList == null) {
            context.drawText(textRenderer, "§cОшибка: список не загружен", formX + 5, listY + 12, 0xFF5555, false);
        } else if (npcList.isEmpty()) {
            context.drawText(textRenderer, "§7Список пуст. Создай первого НПС!", formX + 5, listY + 12, 0x888888, false);
        } else {
            var player = MinecraftClient.getInstance().player;
            int baseY = listY + 12;
            int listBottom = baseY + LIST_VISIBLE_HEIGHT;

            // ✅ Обрезаем всё, что выходит за пределы видимой области
            context.enableScissor(formX, baseY, width - formX, listBottom);

            int displayCount = 0;
            int firstVisibleIndex = (int) Math.ceil(listScrollOffset / LIST_ITEM_HEIGHT);

            for (int i = firstVisibleIndex; i < npcList.size(); i++) {
                NpcEntry npc = npcList.get(i);

                // ✅ ТАКОЙ ЖЕ расчёт как в createListButtons()
                int npcY = baseY + (i * LIST_ITEM_HEIGHT) - (int) listScrollOffset;

                // Прерываем, если вышли за нижнюю границу
                if (npcY >= listBottom) {
                    break;
                }

                // Пропускаем элементы выше видимой области
                if (npcY + LIST_ITEM_HEIGHT < baseY) {
                    displayCount++;
                    continue;
                }

                // Фильтр по радиусу
                if (!showAllNpcs && player != null) {
                    Vec3d npcPos = new Vec3d(npc.x(), npc.y(), npc.z());
                    Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                    double dist = playerPos.distanceTo(npcPos);
                    int radius = mxnder.desertmod.MyConfig.HANDLER.instance().npcRenderRadius;

                    if (dist > radius) {
                        continue; // Не увеличиваем displayCount!
                    }
                }

                // Лимит отображения
                if (displayCount >= MAX_DISPLAY_NPCS) {
                    break;
                }

                String indicator = "○";
                if (player != null) {
                    Vec3d npcPos = new Vec3d(npc.x(), npc.y(), npc.z());
                    Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                    double dist = playerPos.distanceTo(npcPos);
                    int radius = mxnder.desertmod.MyConfig.HANDLER.instance().npcRenderRadius;
                    if (dist <= radius) {
                        indicator = "●";
                    }
                }

                // Фон строки
                context.fill(formX, npcY, width - formX, npcY + LIST_ITEM_HEIGHT, 0x60FFFFFF);

                // Текст
                String line = String.format("%s %s | %s | %s | %.1f %.1f %.1f | %.1f°",
                        indicator, npc.id(), npc.typeKey(), npc.animVariant(),
                        npc.x(), npc.y(), npc.z(), npc.yaw());

                context.drawText(textRenderer, line, formX + 5, npcY + 2, 0xFFFFFFFF, true);

                displayCount++;
            }

            // ✅ Отключаем обрезку
            context.disableScissor();

            // ✅ Рисуем скроллбар
            int totalHeight = npcList.size() * LIST_ITEM_HEIGHT;
            if (totalHeight > LIST_VISIBLE_HEIGHT) {
                int scrollbarWidth = 6;
                int scrollbarX = width - formX - scrollbarWidth;

                context.fill(scrollbarX, baseY, scrollbarX + scrollbarWidth, listBottom, 0x40000000);

                float scrollRatio = (float) listScrollOffset / Math.max(1, totalHeight - LIST_VISIBLE_HEIGHT);
                int thumbHeight = Math.max(20, (int) ((float) LIST_VISIBLE_HEIGHT * LIST_VISIBLE_HEIGHT / totalHeight));
                int thumbY = baseY + (int) (scrollRatio * (LIST_VISIBLE_HEIGHT - thumbHeight));

                context.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, 0xA0888888);
            }

            // Счётчик
            if (npcList.size() > MAX_DISPLAY_NPCS) {
                context.drawText(textRenderer,
                        "§7... (показано " + displayCount + " из " + npcList.size() + ")",
                        formX, listBottom + 5, 0x888888, true);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click mouse, boolean clicked) {
        if (super.mouseClicked(mouse, clicked)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        System.out.println("[SCROLL] Called | v=" + verticalAmount + " at (" + mouseX + "," + mouseY + ")");

        if (npcList == null || npcList.isEmpty()) return false;

        int listY = formY + gap * 3 + 40;
        int listBottom = listY + 12 + LIST_VISIBLE_HEIGHT;

        System.out.println("[SCROLL] List bounds: Y=" + listY + " to " + listBottom);

        if (mouseY >= listY && mouseY <= listBottom && mouseX >= formX && mouseX <= width - formX) {
            System.out.println("[SCROLL] Inside list! offset: " + listScrollOffset);

            int totalHeight = npcList.size() * LIST_ITEM_HEIGHT;
            int maxScroll = Math.max(0, totalHeight - LIST_VISIBLE_HEIGHT);
            listScrollOffset = Math.max(0, Math.min(listScrollOffset - verticalAmount * 15, maxScroll));

            clearListButtons();
            createListButtons();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parentScreen);
    }
}