package mxnder.desertmod;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemFrameInteractionHandler {

    // ЗАМЕНИТЕ ЭТО НАЗВАНИЕ НА ТО, КОТОРОЕ ВАМ НУЖНО (точно как в игре, с учетом регистра и форматирования)
    private static final String TARGET_ITEM_NAME = "Древний меч";

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // Проверяем, является ли сущность рамкой для предметов
            if (entity instanceof ItemFrameEntity itemFrame) {
                // Получаем предмет в рамке
                ItemStack stack = itemFrame.getHeldItemStack();

                if (!stack.isEmpty()) {
                    // Получаем имя предмета (транслированное название)
                    String itemName = stack.getName().getString();

                    // Проверяем совпадение названия
                    if (itemName.equals(TARGET_ITEM_NAME)) {
                        // Отправляем сообщение в чат
                        player.sendMessage(Text.literal("Вы перехватили ПКМ по рамке с предметом: " + itemName), false);

                        // Возвращаем SUCCESS, чтобы предотвратить стандартное действие (например, поворот предмета),
                        // если вы хотите, чтобы действие все же происходило, верните PASS
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}