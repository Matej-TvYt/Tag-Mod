package com.example.tagmod;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class TagBorderEnforcer {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(TagBorderEnforcer::tick);
    }

    private static void tick(MinecraftServer server) {
        if (!TagGame.gameRunning) return;

        double half = TagGame.BORDER_SIZE / 2.0;

        double minX = TagGame.BORDER_CENTER_X - half;
        double maxX = TagGame.BORDER_CENTER_X + half;
        double minZ = TagGame.BORDER_CENTER_Z - half;
        double maxZ = TagGame.BORDER_CENTER_Z + half;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            boolean outside =
                    x < minX || x > maxX ||
                            z < minZ || z > maxZ;

            if (!outside) continue;

            double clampedX = Math.max(minX, Math.min(maxX, x));
            double clampedZ = Math.max(minZ, Math.min(maxZ, z));

            player.requestTeleport(clampedX, y, clampedZ);
        }
    }
}