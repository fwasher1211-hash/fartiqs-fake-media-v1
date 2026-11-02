package com.fakemedia.mixin;

import com.fakemedia.FakeMediaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void fakemedia$addIcon(CallbackInfoReturnable<Text> cir) {
        if (!FakeMediaClient.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || (Object) this != mc.player) return;
        cir.setReturnValue(FakeMediaClient.withIcon(cir.getReturnValue()));
    }
}
