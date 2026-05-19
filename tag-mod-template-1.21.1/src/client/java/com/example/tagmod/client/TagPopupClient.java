package com.example.tagmod.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class TagPopupClient {

    private static long showUntil = 0;
    private static Text currentText = null;

    public static void show(Text text, long durationMs) {
        currentText = text;
        showUntil = System.currentTimeMillis() + durationMs;
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || currentText == null) return;

        if (System.currentTimeMillis() > showUntil) {
            currentText = null;
            return;
        }

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        int textWidth = client.textRenderer.getWidth(currentText);

        context.drawTextWithShadow(
                client.textRenderer,
                currentText,
                width / 2 - textWidth / 2,
                height / 2,
                0xFFFFFF
        );
    }
}