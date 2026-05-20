package com.example.tagmod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TagGame {

    public static boolean gameRunning = false;
    public static UUID chaser = null;

    private static final Random RANDOM = new Random();

    // =====================
    // STATE CHECK
    // =====================
    public static boolean isRunning() {
        return gameRunning;
    }

    public static boolean isChaser(UUID id) {
        return gameRunning && chaser != null && chaser.equals(id);
    }

    // =====================
    // START GAME
    // =====================
    public static void start(MinecraftServer server) {

        List<ServerPlayerEntity> players =
                server.getPlayerManager().getPlayerList();

        if (players.size() < 2) {

            server.getPlayerManager().broadcast(
                    Text.literal("Need at least 2 players to start.")
                            .formatted(Formatting.RED),
                    false
            );

            return;
        }

        // COUNTDOWN
        for (ServerPlayerEntity player : players) {

            player.playSoundToPlayer(
                    SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(),
                    SoundCategory.MASTER,
                    1.0F,
                    0.9F
            );

            player.playSoundToPlayer(
                    SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(),
                    SoundCategory.MASTER,
                    1.0F,
                    1.0F
            );

            player.playSoundToPlayer(
                    SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(),
                    SoundCategory.MASTER,
                    1.0F,
                    1.1F
            );
        }

        gameRunning = true;

        ServerPlayerEntity selected =
                players.get(RANDOM.nextInt(players.size()));

        chaser = selected.getUuid();

        // RAID HORN START SOUND
        for (ServerPlayerEntity player : players) {

            player.playSoundToPlayer(
                    SoundEvents.EVENT_RAID_HORN.value(),
                    SoundCategory.MASTER,
                    1.0F,
                    1.0F
            );
        }

        syncState(server);
    }

    // =====================
    // STOP GAME
    // =====================
    public static void stop(MinecraftServer server) {

        gameRunning = false;
        chaser = null;

        Identifier soundId =
                Identifier.of("tagmod", "game_end");

        for (ServerPlayerEntity player :
                server.getPlayerManager().getPlayerList()) {

            player.playSoundToPlayer(
                    net.minecraft.sound.SoundEvent.of(soundId),
                    SoundCategory.MASTER,
                    1.0F,
                    1.0F
            );
        }
    }

    // =====================
    // ROLE TRANSFER
    // =====================
    public static void transferChase(
            MinecraftServer server,
            UUID newChaser
    ) {

        if (!gameRunning) return;
        if (newChaser == null) return;

        chaser = newChaser;

        // COW BELL SOUND
        for (ServerPlayerEntity player :
                server.getPlayerManager().getPlayerList()) {

            player.playSoundToPlayer(
                    SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL.value(),
                    SoundCategory.MASTER,
                    1.0F,
                    1.0F
            );
        }

        syncState(server);
    }

    // =====================
    // VISUAL/HUD STATE
    // =====================
    public static void syncState(MinecraftServer server) {

        for (ServerPlayerEntity player :
                server.getPlayerManager().getPlayerList()) {

            boolean chaserState =
                    isChaser(player.getUuid());

            player.setGlowing(chaserState);

            if (chaserState) {

                player.sendMessage(
                        Text.literal("You became the CHASER")
                                .formatted(Formatting.RED),
                        true
                );

            } else {

                player.sendMessage(
                        Text.literal("You became a RUNNER")
                                .formatted(Formatting.GREEN),
                        true
                );
            }
        }
    }

    // =====================
    // COMPAT
    // =====================
    public static void applyPlayerState(
            ServerPlayerEntity player
    ) {

        player.setGlowing(
                isChaser(player.getUuid())
        );
    }
}