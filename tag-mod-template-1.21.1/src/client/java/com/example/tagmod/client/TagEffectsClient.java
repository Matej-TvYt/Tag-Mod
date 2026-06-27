package com.example.tagmod.client;

import com.example.tagmod.TagGame;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class TagEffectsClient {

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            ClientPlayerEntity player = MinecraftClient
                    .getInstance()
                    .player;

            if (player == null) {
                return;
            }

            boolean running = TagGame.gameRunning;

            if (!running) {
                player.setGlowing(false);
            }
        });
    }
}