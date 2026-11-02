package com.fakemedia.mixin;

import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DisplayEntity.TextDisplayEntity.class)
public interface TextDisplayAccessor {
    @Invoker("getText")
    Text fakemedia$getText();

    @Invoker("setText")
    void fakemedia$setText(Text text);
}
