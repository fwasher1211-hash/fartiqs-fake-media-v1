package com.fakemedia;

import com.fakemedia.mixin.TextDisplayAccessor;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;

import java.util.List;

public class FakeMediaClient implements ClientModInitializer {
    public static final String ICON_CHAR = "\uE000";
    public static volatile boolean enabled = false;
    public static volatile boolean bypass = false;

    private static Text icon;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
                ClientCommandManager.literal("fakemedia")
                    .executes(ctx -> setState(true, ctx.getSource()))
                    .then(ClientCommandManager.literal("on").executes(ctx -> setState(true, ctx.getSource())))
                    .then(ClientCommandManager.literal("off").executes(ctx -> setState(false, ctx.getSource())))
                    .then(ClientCommandManager.literal("debug").executes(ctx -> debug(ctx.getSource())))
            )
        );
    }

    private static int setState(boolean on, FabricClientCommandSource src) {
        enabled = on;
        refreshDisplays();
        if (on) {
            src.sendFeedback(Text.literal("Fake media rank ON").formatted(Formatting.LIGHT_PURPLE));
        } else {
            src.sendFeedback(Text.literal("Fake media rank OFF").formatted(Formatting.GRAY));
        }
        return 1;
    }

    /** Is this floating text display the one that shows MY name? */
    public static boolean isMine(Entity self, Text raw, MinecraftClient mc) {
        if (mc == null || mc.player == null || raw == null) return false;
        if (self.squaredDistanceTo(mc.player) > 64.0) return false;
        String own = mc.player.getName().getString().toLowerCase();
        return raw.getString().toLowerCase().contains(own);
    }

    /** The original text, without our icon. */
    private static Text rawText(DisplayEntity.TextDisplayEntity td) {
        bypass = true;
        try {
            Text t = ((TextDisplayAccessor) (Object) td).fakemedia$getText();
            return t == null ? Text.empty() : t;
        } finally {
            bypass = false;
        }
    }

    /** Forces the game to rebuild the floating text so the icon appears or disappears right away. */
    public static void refreshDisplays() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        List<DisplayEntity.TextDisplayEntity> list = mc.world.getEntitiesByClass(
            DisplayEntity.TextDisplayEntity.class, mc.player.getBoundingBox().expand(8.0), e -> true);
        for (DisplayEntity.TextDisplayEntity td : list) {
            Text raw = rawText(td);
            if (!isMine(td, raw, mc)) continue;
            TextDisplayAccessor acc = (TextDisplayAccessor) (Object) td;
            acc.fakemedia$setText(Text.literal(" "));
            acc.fakemedia$setText(raw);
        }
    }

    private static int debug(FabricClientCommandSource src) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return 0;
        List<Entity> list = mc.world.getEntitiesByClass(
            Entity.class, mc.player.getBoundingBox().expand(8.0),
            e -> e != mc.player
                && e.getType() != EntityType.ITEM_FRAME
                && e.getType() != EntityType.GLOW_ITEM_FRAME);
        src.sendFeedback(Text.literal("Nearby (no item frames): " + list.size()));
        int shown = 0;
        for (Entity e : list) {
            if (shown++ >= 8) break;
            String line = EntityType.getId(e.getType()).toString();
            if (e instanceof DisplayEntity.TextDisplayEntity td) {
                Text raw = rawText(td);
                line += " text=\"" + raw.getString().replace("\n", " | ") + "\" mine=" + isMine(td, raw, mc);
            } else if (e.hasCustomName() && e.getCustomName() != null) {
                line += " name=\"" + e.getCustomName().getString() + "\"";
            }
            src.sendFeedback(Text.literal(line));
        }
        return 1;
    }

    /** Camera icon, then a normal space, then the original text. Wrapped so the icon font never leaks. */
    public static Text withIcon(Text original) {
        if (original == null) return null;
        if (original.getString().contains(ICON_CHAR)) return original;
        MutableText root = Text.empty();
        root.append(getIcon().copy());
        root.append(Text.literal(" "));
        root.append(original);
        return root;
    }

    private static Text getIcon() {
        if (icon == null) {
            Text parsed = null;
            try {
                String json = "{\"text\":\"" + ICON_CHAR + "\",\"font\":\"fakemedia:media\",\"color\":\"white\"}";
                parsed = TextCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElse(null);
            } catch (Throwable ignored) {
            }
            if (parsed == null) {
                parsed = Text.literal("[Media]").formatted(Formatting.LIGHT_PURPLE);
            }
            icon = parsed;
        }
        return icon;
    }
}
