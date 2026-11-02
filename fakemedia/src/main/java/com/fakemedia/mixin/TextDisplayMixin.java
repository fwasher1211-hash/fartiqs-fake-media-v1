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

/** DonutSMP shows "+ name / $ money" as a floating text display. Put the media icon at the start of YOUR one. */
@Mixin(DisplayEntity.TextDisplayEntity.class)
public abstract class TextDisplayMixin {
    @Inject(method = "getText", at = @At("RETURN"), cancellable = true)
    private void fakemedia$addIcon(CallbackInfoReturnable<Text> cir) {
        if (!FakeMediaClient.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;
        Text original = cir.getReturnValue();
        if (original == null) return;
        String ownName = mc.player.getName().getString();
        if (!original.getString().contains(ownName)) return;
        Entity self = (Entity) (Object) this;
        if (self.squaredDistanceTo(mc.player) > 36.0) return;
        cir.setReturnValue(FakeMediaClient.withIcon(original));
    }
}
