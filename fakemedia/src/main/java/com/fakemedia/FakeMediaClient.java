package com.fakemedia;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;

public class FakeMediaClient implements ClientModInitializer {
    public static final String ICON_CHAR = "\uE000";
    public static volatile boolean enabled = false;

    private static Text icon;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
                ClientCommandManager.literal("fakemedia")
                    .executes(ctx -> {
                        enabled = true;
                        ctx.getSource().sendFeedback(Text.literal("Fake media rank ON").formatted(Formatting.LIGHT_PURPLE));
                        return 1;
                    })
                    .then(ClientCommandManager.literal("on").executes(ctx -> {
                        enabled = true;
                        ctx.getSource().sendFeedback(Text.literal("Fake media rank ON").formatted(Formatting.LIGHT_PURPLE));
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("off").executes(ctx -> {
                        enabled = false;
                        ctx.getSource().sendFeedback(Text.literal("Fake media rank OFF").formatted(Formatting.GRAY));
                        return 1;
                    }))
            )
        );
    }

    /** Puts the media icon at the very front of the name, in a plain wrapper so the icon font doesn't leak onto the name. */
    public static Text withIcon(Text original) {
        if (original == null) return null;
        if (original.getString().contains(ICON_CHAR)) return original;
        MutableText root = Text.empty();
        root.append(getIcon().copy());
        root.append(original);
        return root;
    }

    private static Text getIcon() {
        if (icon == null) {
            Text parsed = null;
            try {
                String json = "{\"text\":\"" + ICON_CHAR + " \",\"font\":\"fakemedia:media\",\"color\":\"white\"}";
                parsed = TextCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElse(null);
            } catch (Throwable ignored) {
            }
            if (parsed == null) {
                parsed = Text.literal("[Media] ").formatted(Formatting.LIGHT_PURPLE);
            }
            icon = parsed;
        }
        return icon;
    }
}
