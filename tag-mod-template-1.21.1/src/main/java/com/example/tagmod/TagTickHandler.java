package com.example.tagmod;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class TagTickHandler {

    public static void register() {

        ServerTickEvents.END_SERVER_TICK.register(server -> {

            if (!TagGame.isRunning()) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.setGlowing(TagGame.isChaser(player.getUuid()));
            }
        });
    }
}