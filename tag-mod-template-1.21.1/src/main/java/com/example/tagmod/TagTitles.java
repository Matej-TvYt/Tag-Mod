package com.example.tagmod;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TagTitles {

    public static void showRunner(ServerPlayerEntity player) {
        player.sendMessage(
                Text.literal("You became a RUNNER")
                        .formatted(Formatting.GREEN),
                true
        );
    }

    public static void showChaser(ServerPlayerEntity player) {
        player.sendMessage(
                Text.literal("You became a CHASER")
                        .formatted(Formatting.RED),
                true
        );
    }
}