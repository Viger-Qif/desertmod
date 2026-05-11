package mxnder.desertmod.screen;

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
    private static final List<String> NPC_TYPES = List.of("simple", "lamberjack");
    private static final Map<String, List<String>> ANIMS_BY_TYPE = Map.of(
            "simple", List.of("idle_hat", "idle_hair", "lean_1_hair", "talk_hair", "sit_1"),
            "example", List.of("idle", "work in progress")
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
        if (npcList == null) return;

        var player = MinecraftClient.getInstance().player;
        int listY = formY + gap * 3 + 40;
        int npcY = listY + 12;
        int displayCount = 0;

        for (NpcEntry npc : npcList) {
            // Фильтр по радиусу (такой же, как в render())
            if (!showAllNpcs && player != null) {
                Vec3d npcPos = new Vec3d(npc.x(), npc.y(), npc.z());
                Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                double dist = playerPos.distanceTo(npcPos);
                int radius = mxnder.desertmod.MyConfig.HANDLER.instance().npcRenderRadius;
                if (dist > radius) continue;
            }
            if (displayCount >= MAX_DISPLAY_NPCS) break;

            // === Вычисляем правую границу контента (симметрично formX) ===
            int rightEdge = width - formX;  // Например: width - 50

            // Кнопка [edit] — внутри строки, справа
            ButtonWidget editBtn = ButtonWidget.builder(
                            Text.literal("edit"),
                            btn -> editNpc(npc)
                    )
                    .position(rightEdge - 85, npcY)  // ✅ 35px кнопка + 5px отступ + 30px del
                    .size(35, LIST_ITEM_HEIGHT)
                    .build();
            addDrawableChild(editBtn);
            listButtons.add(editBtn);

            // Кнопка [del] — в самом краю, внутри строки
            ButtonWidget delBtn = ButtonWidget.builder(
                            Text.literal("del"),
                            btn -> deleteNpc(npc.id())
                    )
                    .position(rightEdge - 45, npcY)  // ✅ 30px кнопка, прижата к правому краю
                    .size(30, LIST_ITEM_HEIGHT)
                    .build();
            addDrawableChild(delBtn);
            listButtons.add(delBtn);

            npcY += LIST_ITEM_HEIGHT;
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
        if (System.currentTimeMillis() - lastListRefresh > 1000) {
            npcList = NpcDataManager.loadNpcs();
            lastListRefresh = System.currentTimeMillis();
        }
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

            sendMessage("§a[✔] НПС сохранён: " + id);
            clearForm();
            playClickSound();

            init();

            // ... остальной код ...
        } catch (Exception e) {
            sendMessage("§c[Ошибка] " + e.getMessage());
            e.printStackTrace();  // ✅ Важная строка!
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

        sendMessage("§a[✔] НПС удалён: " + id);
        if (editingEntry != null && editingEntry.id().equals(id)) {
            clearForm();
        }
        playClickSound();
        init();
    }

    private void clearListButtons() {
        for (ButtonWidget btn : listButtons) {
            remove(btn);  // Убираем виджет с экрана
        }
        listButtons.clear();  // Очищаем список
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            if (text.isEmpty()) return defaultValue;
            // ✅ Заменяем запятую на точку
            return Double.parseDouble(text.replace(',', '.'));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private float parseFloat(String text, float defaultValue) {
        try {
            if (text.isEmpty()) return defaultValue;
            // ✅ Заменяем запятую на точку
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
        int listY = formY + gap * 3 + 40;  // ✅ ОДИН РАЗ определяем
        context.drawText(textRenderer, "📋 Список НПС", formX, listY, 0xFFFFA0, true);

        // Рисуем список НПС
        if (npcList == null) {
            context.drawText(textRenderer, "§cОшибка: список не загружен", formX + 5, listY + 12, 0xFF5555, false);
        } else if (npcList.isEmpty()) {
            context.drawText(textRenderer, "§7Список пуст. Создай первого НПС!", formX + 5, listY + 12, 0x888888, false);
        } else {
            var player = MinecraftClient.getInstance().player;
            int npcY = listY + 12;  // ✅ ИСПОЛЬЗУЕМ уже определённый listY
            int displayCount = 0;

            for (int i = 0; i < npcList.size(); i++) {
                NpcEntry npc = npcList.get(i);

                // Фильтр по радиусу
                if (!showAllNpcs && player != null) {
                    Vec3d npcPos = new Vec3d(npc.x(), npc.y(), npc.z());
                    Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                    double dist = playerPos.distanceTo(npcPos);
                    int radius = mxnder.desertmod.MyConfig.HANDLER.instance().npcRenderRadius;

                    if (dist > radius) {
                        continue;  // Пропускаем вне радиуса
                    }
                }

                if (displayCount >= MAX_DISPLAY_NPCS) break;

                // Индикатор
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

                // Белый цвет + shadow=true
                context.drawText(textRenderer, line, formX + 5, npcY + 2, 0xFFFFFFFF, true);

                npcY += LIST_ITEM_HEIGHT;
                displayCount++;
            }

            // Если НПС больше, чем показали
            if (npcList.size() > MAX_DISPLAY_NPCS) {
                context.drawText(textRenderer,
                        "§7... (показано " + displayCount + " из " + npcList.size() + ")",
                        formX, npcY + 2, 0x888888, true);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click mouse, boolean clicked) {
        if (super.mouseClicked(mouse, clicked)) {
            return true;
        }

        if (!clicked || npcList == null) {
            return false;
        }

        double mouseX = mouse.x();
        double mouseY = mouse.y();

        var player = MinecraftClient.getInstance().player;
        int listY = formY + gap * 3 + 40;
        int npcY = listY + 12;
        int displayCount = 0;

        for (NpcEntry npc : npcList) {
            // ... фильтр по радиусу ...
            if (!showAllNpcs && player != null) {
                Vec3d npcPos = new Vec3d(npc.x(), npc.y(), npc.z());
                Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                double dist = playerPos.distanceTo(npcPos);
                int radius = mxnder.desertmod.MyConfig.HANDLER.instance().npcRenderRadius;
                if (dist > radius) continue;
            }
            if (displayCount >= MAX_DISPLAY_NPCS) break;

            npcY += LIST_ITEM_HEIGHT;
            displayCount++;
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