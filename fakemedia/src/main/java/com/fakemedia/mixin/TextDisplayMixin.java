package com.fakemedia.mixin;

import com.fakemedia.FakeMediaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DisplayEntity.TextDisplayEntity.class)
public abstract class TextDisplayMixin {
    @Inject(method = "getText", at = @At("RETURN"), cancellable = true)
    private void fakemedia$addIcon(CallbackInfoReturnable<Text> cir) {
        if (!FakeMediaClient.enabled || FakeMediaClient.bypass) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;
        Text original = cir.getReturnValue();
        if (original == null) return;
        Entity self = (Entity) (Object) this;
        if (!FakeMediaClient.isMine(self, original, mc)) return;
        cir.setReturnValue(FakeMediaClient.withIcon(original));
    }
}
