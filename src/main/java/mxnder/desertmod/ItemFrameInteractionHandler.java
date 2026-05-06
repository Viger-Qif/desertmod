package mxnder.desertmod;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Map;

public class ItemFrameInteractionHandler {

    // название предмета -> сообщение для чата
    private static final Map<String, String> ITEM_MESSAGES = new HashMap<>();

    static {
        ITEM_MESSAGES.put("grassik", "Эта трава не из полей и не из реки. Её выращивают в глубине, где нужен не свет, а только тишина и влага.");
        ITEM_MESSAGES.put("kaplya", "Зелень рождается там, где вода стекает по камню. Здесь можно почувствовать дыхание подземного сада.");
        ITEM_MESSAGES.put("kaplyi", "Три капли означают, что влага здесь не случайна. Рядом в тени растёт мох, чьи корни пьют из Великого Нила.");
        // Добавьте другие предметы по аналогии:
        // ITEM_MESSAGES.put("Название предмета", "Ваше сообщение в чат");
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // Работаем только на клиенте
            if (!world.isClient()) {
                return ActionResult.PASS;
            }

            // Проверяем, является ли сущность рамкой для предметов
            if (entity instanceof ItemFrameEntity itemFrame) {
                // Получаем предмет в рамке
                ItemStack stack = itemFrame.getHeldItemStack();

                if (!stack.isEmpty()) {
                    // Получаем имя предмета (транслированное название)
                    String itemName = stack.getName().getString();

                    // Проверяем, есть ли сообщение для этого предмета
                    String message = ITEM_MESSAGES.get(itemName);

                    if (message != null) {
                        // Отправляем сообщение в чат
                        player.sendMessage(Text.literal(message), false);

                        // Возвращаем SUCCESS, чтобы предотвратить стандартное действие (например, поворот предмета),
                        // если вы хотите, чтобы действие все же происходило, верните PASS
                        return ActionResult.SUCCESS;
                    }

                    // Особая логика для предмета "papirus"
                    if ("papirus".equals(itemName)) {
                        // Останавливаем вращение предмета в рамке
                        itemFrame.setInvisible(false); // предмет остаётся видимым

                        // Открываем экран с изображением папируса
                        MinecraftClient.getInstance().setScreen(new mxnder.desertmod.screen.PapirusScreen());

                        // Возвращаем SUCCESS, чтобы предотвратить стандартное вращение
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;

        });
    }
}