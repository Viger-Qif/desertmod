package mxnder.desertmod.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Пример Mixin для MinecraftServer (заглушка)
@Mixin(MinecraftServer.class)
public class ExampleMixin {
    // Вызывается при загрузке мира
    @Inject(at = @At("HEAD"), method = "loadWorld")
    private void init(CallbackInfo info) {
    }
}
