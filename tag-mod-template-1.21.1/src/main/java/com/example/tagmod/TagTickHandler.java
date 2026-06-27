package com.example.tagmod;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TagTickHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> TagGame.tick(server));
    }
}