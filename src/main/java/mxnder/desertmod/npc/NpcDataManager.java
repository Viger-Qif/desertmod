package mxnder.desertmod.npc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Менеджер для хранения и управления данными NPC.
 * Отвечает за загрузку/сохранение списка NPC в JSON файл.
 *
 * Файл хранится в конфиге мода: config/desertmod_npcs.json
 * Формат JSON с отступами для удобного ручного редактирования.
 */
@Environment(EnvType.CLIENT)
public class NpcDataManager {

    // Путь к файлу сохранения NPC
    private static final String NPC_CONFIG_PATH = "desertmod_npcs.json";

    // Gson с красивым форматированием (отступы для читаемости)
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    // Кэш загруженных данных
    private static List<NpcEntry> npcCache = null;

    // File watcher для отслеживания изменений в файле
    private static WatchService watchService = null;
    private static Thread watchThread = null;
    private static final AtomicBoolean watchStarted = new AtomicBoolean(false);
    private static Path npcConfigPath = null;

    /**
     * Загружает список NPC из JSON файла.
     * Если файл не существует или повреждён - возвращает пустой список.
     *
     * @return Список всех сохранённых NPC
     */
    public static synchronized List<NpcEntry> loadNpcs() {
        if (npcCache != null) {
            return new ArrayList<>(npcCache); // Возвращаем копию кэша
        }

        File configFile = getConfigFile();

        if (!configFile.exists()) {
            npcCache = new ArrayList<>();
            return new ArrayList<>(npcCache);
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<NpcEntry>>(){}.getType();
            List<NpcEntry> loaded = GSON.fromJson(reader, listType);
            npcCache = (loaded != null) ? loaded : new ArrayList<>();
            return new ArrayList<>(npcCache);
        } catch (IOException e) {
            System.err.println("[DesertMod] Ошибка при загрузке NPC конфига: " + e.getMessage());
            npcCache = new ArrayList<>();
            return new ArrayList<>(npcCache);
        }
    }

    /**
     * Сохраняет весь список NPC в JSON файл.
     * Перезаписывает старый файл полностью.
     *
     * @param npcs Список NPC для сохранения
     * @return true если успешно, false если ошибка
     */
    public static synchronized boolean saveNpcs(List<NpcEntry> npcs) {
        File configFile = getConfigFile();

        try {
            // Создаём родительские директории если нужно
            configFile.getParentFile().mkdirs();

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                GSON.toJson(npcs, writer);
                npcCache = new ArrayList<>(npcs); // Обновляем кэш
                return true;
            }
        } catch (IOException e) {
            System.err.println("[DesertMod] Ошибка при сохранении NPC конфига: " + e.getMessage());
            return false;
        }
    }

    /**
     * Сохраняет или обновляет одного NPC.
     * Если NPC с таким ID уже существует — обновляет его.
     * Если нет — добавляет нового.
     *
     * @param entry NPC для сохранения
     */
    public static synchronized void saveNpc(NpcEntry entry) {
        List<NpcEntry> npcs = loadNpcs();

        // Проверяем, есть ли NPC с таким ID (обновление)
        boolean found = false;
        for (int i = 0; i < npcs.size(); i++) {
            if (npcs.get(i).id().equals(entry.id())) {
                npcs.set(i, entry);  // Обновляем существующего
                found = true;
                break;
            }
        }

        // Если не нашли — добавляем нового
        if (!found) {
            npcs.add(entry);
        }

        // Сохраняем весь список
        saveNpcs(npcs);
    }

    /**
     * Удаляет NPC по ID.
     *
     * @param id ID NPC для удаления
     */
    public static synchronized void deleteNpc(String id) {
        List<NpcEntry> npcs = loadNpcs();
        npcs.removeIf(npc -> npc.id().equals(id));
        saveNpcs(npcs);
    }

    /**
     * Проверяет, существует ли NPC с таким ID в списке.
     *
     * @param npcs Список NPC для проверки
     * @param id ID для проверки
     * @param excludeId ID который нужно игнорировать (для режима редактирования)
     * @return true если дубликат найден
     */
    public static boolean hasDuplicateId(List<NpcEntry> npcs, String id, String excludeId) {
        for (NpcEntry entry : npcs) {
            if (entry.id().equals(id) && !entry.id().equals(excludeId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Генерирует уникальный ID для нового NPC на основе типа.
     * Формат: type_01, type_02, и т.д.
     *
     * @param npcs Текущий список NPC
     * @param type Тип NPC (например "simple", "example")
     * @return Уникальный ID
     */
    public static String generateUniqueId(List<NpcEntry> npcs, String type) {
        int maxNum = 0;

        for (NpcEntry entry : npcs) {
            if (entry.id().startsWith(type + "_")) {
                try {
                    int num = Integer.parseInt(entry.id().substring(type.length() + 1));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return String.format("%s_%02d", type, maxNum + 1);
    }

    /**
     * Находит NPC по ID в списке.
     *
     * @param npcs Список NPC
     * @param id ID для поиска
     * @return Найденный NPC или null
     */
    public static NpcEntry findById(List<NpcEntry> npcs, String id) {
        for (NpcEntry entry : npcs) {
            if (entry.id().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    // Получает путь к файлу конфигурации NPC
    private static File getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve(NPC_CONFIG_PATH).toFile();
    }

    // Очищает кэш (при выходе из мира например)
    public static void clearCache() {
        npcCache = null;
    }

    /**
     * Запускает FileWatcher для отслеживания изменений в JSON файле NPC.
     * Вызывается при инициализации клиента.
     */
    public static void startFileWatcher() {
        if (!watchStarted.compareAndSet(false, true)) {
            return; // Уже запущен
        }

        npcConfigPath = getConfigFile().toPath();

        try {
            watchService = FileSystems.getDefault().newWatchService();
            npcConfigPath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            watchThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        break;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path changedFile = (Path) event.context();
                            if (changedFile.getFileName().toString().equals(NPC_CONFIG_PATH)) {
                                // Файл изменён - обновляем кэш и вызываем refresh
                                System.out.println("[DesertMod] NPC config file changed, refreshing NPCs...");
                                npcCache = null; // Сбрасываем кэш

                                // Вызываем refresh NPC на клиенте
                                ClientNpcSpawner.refreshAllNpcs();
                            }
                        }
                    }

                    key.reset();
                }
            }, "DesertMod-NPC-FileWatcher");
            watchThread.setDaemon(true);
            watchThread.start();

            System.out.println("[DesertMod] NPC FileWatcher started for: " + npcConfigPath);
        } catch (IOException e) {
            System.err.println("[DesertMod] Failed to start NPC FileWatcher: " + e.getMessage());
            watchStarted.set(false);
        }
    }

    /**
     * Останавливает FileWatcher.
     * Вызывается при закрытии игры.
     */
    public static void stopFileWatcher() {
        if (watchThread != null) {
            watchThread.interrupt();
            watchThread = null;
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                // Игнорируем
            }
            watchService = null;
        }
        watchStarted.set(false);
    }
}
