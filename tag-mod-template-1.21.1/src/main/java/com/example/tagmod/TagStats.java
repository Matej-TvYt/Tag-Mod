package com.example.tagmod;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class TagStats {

    // ===== GAME STATE =====
    public static boolean roundActive = false;

    public static long roundStartTime = 0;

    // Single chaser system
    public static UUID chaserId = null;

    // Stats
    public static final Map<UUID, Integer> totalTags = new HashMap<>();
    public static final Map<UUID, Long> survivalTimes = new HashMap<>();

    public static long fastestTagTime = -1;

    // ===== GAME CONTROL =====

    /**
     * Starts a round and assigns initial chaser.
     */
    public static void startGame(List<ServerPlayerEntity> players) {
        roundActive = true;
        roundStartTime = System.currentTimeMillis();

        totalTags.clear();
        survivalTimes.clear();

        fastestTagTime = -1;

        // pick first chaser (simple deterministic rule)
        if (!players.isEmpty()) {
            chaserId = players.get(0).getUuid();
        } else {
            chaserId = null;
        }
    }

    public static void endRound() {
        roundActive = false;
    }

    // ===== ROLE SYSTEM =====

    public static boolean isChaser(UUID uuid) {
        return chaserId != null && chaserId.equals(uuid);
    }

    public static boolean isChaser(ServerPlayerEntity player) {
        return isChaser(player.getUuid());
    }

    public static void setChaser(UUID newChaser) {
        chaserId = newChaser;
    }

    public static void setChaser(ServerPlayerEntity player) {
        chaserId = player.getUuid();
    }

    // ===== TAG LOGIC =====

    public static void addTag(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();

        totalTags.put(uuid, totalTags.getOrDefault(uuid, 0) + 1);

        long time = System.currentTimeMillis() - roundStartTime;

        if (fastestTagTime == -1 || time < fastestTagTime) {
            fastestTagTime = time;
        }
    }

    /**
     * FIXED: now matches your TagGame usage:
     * TagStats.saveSurvivalTime(player, time)
     */
    public static void saveSurvivalTime(ServerPlayerEntity player, long time) {
        survivalTimes.put(player.getUuid(), time);
    }

    public static long getSurvivalTime(ServerPlayerEntity player) {
        return survivalTimes.getOrDefault(player.getUuid(),
                System.currentTimeMillis() - roundStartTime);
    }

    // ===== ROLE SWAP (core mechanic fix) =====

    public static void swapChaser(ServerPlayerEntity oldChaser, ServerPlayerEntity newChaser) {
        if (oldChaser != null) {
            long survival = System.currentTimeMillis() - roundStartTime;
            saveSurvivalTime(oldChaser, survival);
        }

        if (newChaser != null) {
            setChaser(newChaser);
        }
    }

    // ===== STATS =====

    public static String getLongestSurvivor() {
        UUID best = null;
        long bestTime = 0;

        for (Map.Entry<UUID, Long> e : survivalTimes.entrySet()) {
            if (e.getValue() > bestTime) {
                bestTime = e.getValue();
                best = e.getKey();
            }
        }

        if (best == null) return "None";

        return best + " - " + (bestTime / 1000) + "s";
    }
}