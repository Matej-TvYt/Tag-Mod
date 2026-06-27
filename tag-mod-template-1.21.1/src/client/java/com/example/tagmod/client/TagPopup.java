package com.example.tagmod.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public class TagPopup {

    private static String text = null;
    private static long endTime = 0;
    private static int color = 0xFFFFFF;

    public static void show(String msg, int colorRgb) {
        text = msg;
        color = colorRgb;
        endTime = System.currentTimeMillis() + 1000;
    }

    public static void register() {

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {

            if (text == null) return;
            if (System.currentTimeMillis() > endTime) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            TextRenderer tr = client.textRenderer;

            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();

            float x = width / 2f - tr.getWidth(text) / 2f;
            float y = height / 2f;

            drawContext.drawText(
                    tr,
                    text,
                    (int) x,
                    (int) y,
                    color,
                    true
            );
        });
    }
}