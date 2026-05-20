package com.example.tagmod.client;

import com.example.tagmod.TagGame;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TagEffectsClient {

    public static void init() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            boolean running = TagGame.isRunning();

            for (PlayerEntity player : client.world.getPlayers()) {

                // BEFORE GAME START → NO GLOW EVER
                if (!running) {
                    player.setGlowing(false);
                    continue;
                }

                // AFTER START → ONLY CHASER GLOWS
                if (TagGame.isChaser(player.getUuid())) {
                    player.setGlowing(true);
                } else {
                    player.setGlowing(false);
                }
            }
        });
    }
}