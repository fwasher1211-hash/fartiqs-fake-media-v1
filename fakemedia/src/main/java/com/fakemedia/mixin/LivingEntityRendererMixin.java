package com.fakemedia.mixin;

import com.fakemedia.FakeMediaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vanilla never draws your own nametag. When fakemedia is on, draw it in third person so the icon is visible. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true, require = 0)
    private void fakemedia$showOwnTag(LivingEntity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (!FakeMediaClient.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || entity != mc.player) return;
        if (mc.options.getPerspective().isFirstPerson()) return;
        cir.setReturnValue(true);
    }
}
