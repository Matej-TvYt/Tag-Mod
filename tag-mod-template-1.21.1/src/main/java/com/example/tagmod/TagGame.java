package com.example.tagmod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class TagGame {

    // ===== GAME STATE =====
    public static boolean gameRunning = false;
    public static UUID chaser;

    // ===== BORDER =====
    public static final double BORDER_SIZE = 80.0;
    public static final double BORDER_CENTER_X = 0.0;
    public static final double BORDER_CENTER_Z = 0.0;

    // ===== SPAWN PROTECTION =====
    private static final Set<UUID> spawnProtected = new HashSet<>();

    // ===== START GAME (NO TELEPORT VERSION) =====
    public static void start(MinecraftServer server) {
        if (gameRunning) return;

        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        if (players.size() < 2) return;

        gameRunning = true;

        ServerPlayerEntity first = players.get(new Random().nextInt(players.size()));
        setChaser(server, first.getUuid());

        // IMPORTANT: no teleporting players anymore
        syncState(server);
    }

    // ===== ROLE CONTROL =====
    public static void setChaser(MinecraftServer server, UUID newChaser) {
        chaser = newChaser;
        syncState(server);
    }

    public static boolean isChaser(UUID uuid) {
        return gameRunning && chaser != null && chaser.equals(uuid);
    }

    // ===== VISUAL SYNC ONLY (NO MOVEMENT) =====
    public static void syncState(MinecraftServer server) {
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {

            boolean isChaser = p.getUuid().equals(chaser);

            // only visual state
            p.setGlowing(isChaser);

            if (isChaser) {
                TagTitles.showChaser(p);
            } else {
                TagTitles.showRunner(p);
            }
        }
    }

    // ===== TAG EVENT =====
    public static void onTagged(MinecraftServer server, UUID newChaser) {
        setChaser(server, newChaser);
    }

    // ===== SPAWN PROTECTION =====
    public static void giveProtection(UUID uuid) {
        spawnProtected.add(uuid);
    }

    public static boolean hasProtection(UUID uuid) {
        return spawnProtected.contains(uuid);
    }

    public static void clearProtection(UUID uuid) {
        spawnProtected.remove(uuid);
    }
}