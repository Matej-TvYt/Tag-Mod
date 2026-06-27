package com.example.tagmod;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;

import java.util.*;

public class TagGame {

    // =========================
    // MODES
    // =========================

    public static boolean freezeMode = false;
    public static boolean killerMode = false;
    public static boolean gameRunning = false;

    // =========================
    // NORMAL MODE
    // =========================

    public static UUID chaser = null;

    // =========================
    // KILLER MODE
    // =========================

    public static UUID killer = null;
    public static UUID lastKiller = null;

    // =========================
    // FREEZE MODE
    // =========================

    public static final Set<UUID> FREEZERS = new HashSet<>();
    public static final Set<UUID> FROZEN = new HashSet<>();
    public static final Map<UUID, Long> FROZEN_SINCE = new HashMap<>();

    // =========================
    // TRACKING
    // =========================

    public static final Map<UUID, Long> RUNNER_START = new HashMap<>();
    public static final Map<UUID, String> PLAYER_NAMES = new HashMap<>();

    public static UUID longestSurvivor = null;
    public static long longestSurvivalTime = 0;

    public static boolean roundEnding = false;
    public static long roundEndStart = 0;

    // restart countdown
    public static boolean restarting = false;
    public static long restartStartMs = 0;
    public static int restartCountdown = 5;

    private static final Random RANDOM = new Random();

    private static final String TEAM_CHASER  = "tag_chaser";
    private static final String TEAM_FREEZER = "tag_freezer";
    private static final String TEAM_KILLER  = "tag_killer";
    private static final String TEAM_RUNNER  = "tag_runner";

    // =========================
    // CHECKS
    // =========================

    public static boolean isFreezeMode() { return freezeMode && !killerMode; }
    public static boolean isKillerMode() { return killerMode && !freezeMode; }
    public static boolean isFreezer(UUID id) { return FREEZERS.contains(id); }
    public static boolean isFrozen(UUID id) { return FROZEN.contains(id); }
    public static boolean isKiller(UUID id) { return killer != null && killer.equals(id); }
    public static boolean isChaser(UUID id) { return chaser != null && chaser.equals(id); }

    public static boolean isRunner(UUID id) {
        if (killerMode) return !isKiller(id);
        if (freezeMode) return !isFreezer(id);
        return !isChaser(id);
    }

    private static int neededPlayers() {
        // killer and freeze need 2+, normal needs 2+
        return 2;
    }

    // =========================
    // START
    // =========================

    public static void start(MinecraftServer server) {

        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        if (gameRunning) return;

        if (players.size() < 2) {
            server.getPlayerManager().broadcast(
                    Text.literal("Need at least 2 players to start.").formatted(Formatting.RED),
                    false
            );
            return;
        }

        reset(server);

        gameRunning = true;

        PLAYER_NAMES.clear();
        RUNNER_START.clear();

        for (ServerPlayerEntity p : players) {
            PLAYER_NAMES.put(p.getUuid(), p.getName().getString());
        }

        ServerPlayerEntity selected = players.get(RANDOM.nextInt(players.size()));

        // KILLER MODE
        if (killerMode) {

            killer = selected.getUuid();
            lastKiller = killer;

            for (ServerPlayerEntity p : players) {
                if (!isKiller(p.getUuid())) {
                    RUNNER_START.put(p.getUuid(), System.currentTimeMillis());
                }
            }

            server.getPlayerManager().broadcast(
                    Text.literal(selected.getName().getString() + " is the KILLER!").formatted(Formatting.RED),
                    false
            );

            for (ServerPlayerEntity p : players) {
                if (isKiller(p.getUuid())) {
                    sendTitle(p,
                            Text.literal("YOU ARE THE KILLER").formatted(Formatting.RED),
                            Text.literal("Kill everyone!").formatted(Formatting.GRAY));
                } else {
                    sendTitle(p,
                            Text.literal("YOU ARE A RUNNER").formatted(Formatting.GREEN),
                            Text.literal("Survive!").formatted(Formatting.GRAY));
                }
            }

            syncGlowing(server);
            return;
        }

        // FREEZE MODE
        if (freezeMode) {

            FREEZERS.add(selected.getUuid());

            for (ServerPlayerEntity p : players) {
                if (!p.getUuid().equals(selected.getUuid())) {
                    RUNNER_START.put(p.getUuid(), System.currentTimeMillis());
                }
            }

            server.getPlayerManager().broadcast(
                    Text.literal(selected.getName().getString() + " is the FREEZER!").formatted(Formatting.AQUA),
                    false
            );

            for (ServerPlayerEntity p : players) {
                if (isFreezer(p.getUuid())) {
                    sendTitle(p,
                            Text.literal("YOU ARE THE FREEZER").formatted(Formatting.AQUA),
                            Text.literal("Freeze everyone!").formatted(Formatting.GRAY));
                } else {
                    sendTitle(p,
                            Text.literal("YOU ARE A RUNNER").formatted(Formatting.GREEN),
                            Text.literal("Avoid the freezer!").formatted(Formatting.GRAY));
                }
            }

            syncGlowing(server);
            return;
        }

        // NORMAL MODE
        chaser = selected.getUuid();

        for (ServerPlayerEntity p : players) {
            if (!p.getUuid().equals(chaser)) {
                RUNNER_START.put(p.getUuid(), System.currentTimeMillis());
            }
        }

        server.getPlayerManager().broadcast(
                Text.literal(selected.getName().getString() + " is the CHASER!").formatted(Formatting.RED),
                false
        );

        for (ServerPlayerEntity p : players) {
            if (isChaser(p.getUuid())) {
                sendTitle(p,
                        Text.literal("YOU ARE THE CHASER").formatted(Formatting.RED),
                        Text.literal("Chase someone!").formatted(Formatting.GRAY));
            } else {
                sendTitle(p,
                        Text.literal("YOU ARE A RUNNER").formatted(Formatting.GREEN),
                        Text.literal("Run away!").formatted(Formatting.GRAY));
            }
        }

        syncGlowing(server);
    }

    // =========================
    // NORMAL MODE TAG
    // =========================

    public static void transferChase(MinecraftServer server, UUID newChaser) {
        if (!gameRunning) return;
        if (freezeMode) return;
        if (killerMode) return;
        chaser = newChaser;
        syncGlowing(server);
    }

    // =========================
    // FREEZE
    // =========================

    public static void freeze(ServerPlayerEntity target) {

        if (!gameRunning || !freezeMode) return;

        UUID id = target.getUuid();
        if (FROZEN.contains(id)) return;

        FROZEN.add(id);
        TagIceInteractHandler.FREEZE_TIMERS.put(id, System.currentTimeMillis());

        buildIceCage(target);
    }

    public static void unfreeze(ServerPlayerEntity target) {

        UUID id = target.getUuid();
        if (!FROZEN.contains(id)) return;

        FROZEN.remove(id);
        TagIceInteractHandler.FREEZE_TIMERS.remove(id);
    }

    // =========================
    // ROLE LEFT — triggers restart countdown (fix 1)
    // =========================

    public static void roleLeft(MinecraftServer server, String playerName, UUID id) {

        if (!gameRunning) return;

        boolean keyPlayerLeft = false;

        if (killerMode && isKiller(id)) keyPlayerLeft = true;
        if (freezeMode && isFreezer(id)) keyPlayerLeft = true;
        if (!killerMode && !freezeMode && isChaser(id)) keyPlayerLeft = true;

        if (!keyPlayerLeft) return;

        int currentPlayers = server.getPlayerManager().getPlayerList().size();

        // check if enough players remain to restart
        if (currentPlayers < 2) {
            // not enough — end round, tell everyone
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                sendTitle(p,
                        Text.literal("Round Failed!").formatted(Formatting.RED),
                        Text.literal("Failed to restart: need at least 2 people!").formatted(Formatting.GRAY));
            }

            server.getPlayerManager().broadcast(
                    Text.literal(playerName + " left the game. Not enough players to restart.").formatted(Formatting.RED),
                    false
            );

            stop(server);
            return;
        }

        // enough players — start restart countdown
        server.getPlayerManager().broadcast(
                Text.literal(playerName + " left the game. Round restarting...").formatted(Formatting.YELLOW),
                false
        );

        // stop current round state but keep gameRunning false while counting down
        softReset(server);

        restarting = true;
        restartStartMs = System.currentTimeMillis();
        restartCountdown = 5;

        // show countdown on all screens
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            sendTitle(p,
                    Text.literal("Round Restarting").formatted(Formatting.YELLOW),
                    Text.literal("Starting in 5...").formatted(Formatting.GRAY));
        }
    }

    // =========================
    // TICK
    // =========================

    public static void tick(MinecraftServer server) {

        // handle restart countdown (fix 1)
        if (restarting) {

            long elapsed = System.currentTimeMillis() - restartStartMs;
            int secondsLeft = 5 - (int)(elapsed / 1000);

            if (secondsLeft != restartCountdown) {
                restartCountdown = secondsLeft;

                if (restartCountdown > 0) {
                    for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                        p.networkHandler.sendPacket(new OverlayMessageS2CPacket(
                                Text.literal("Restarting in " + restartCountdown + "s...").formatted(Formatting.YELLOW)
                        ));
                    }
                }
            }

            if (elapsed >= 5000) {
                restarting = false;

                int currentPlayers = server.getPlayerManager().getPlayerList().size();
                if (currentPlayers < 2) {
                    server.getPlayerManager().broadcast(
                            Text.literal("Not enough players to restart. Round cancelled.").formatted(Formatting.RED),
                            false
                    );
                    for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                        sendTitle(p,
                                Text.literal("Round Cancelled!").formatted(Formatting.RED),
                                Text.literal("Need at least 2 players!").formatted(Formatting.GRAY));
                    }
                    return;
                }

                start(server);
            }

            return;
        }

        if (!gameRunning) return;

        syncGlowing(server);

        // KILLER MODE WIN CHECK
        if (killerMode) {

            int alive = 0;
            long longestTime = 0;
            String longestName = "Unknown";

            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                if (!isKiller(p.getUuid()) && !p.isSpectator()) {
                    alive++;
                }
            }

            for (Map.Entry<UUID, Long> entry : RUNNER_START.entrySet()) {
                UUID id = entry.getKey();
                if (isKiller(id)) continue;
                long survived = System.currentTimeMillis() - entry.getValue();
                if (survived > longestTime) {
                    longestTime = survived;
                    longestName = PLAYER_NAMES.getOrDefault(id, "Unknown");
                }
            }

            if (alive == 0) {

                long seconds = longestTime / 1000;
                long minutes = seconds / 60;
                long secs = seconds % 60;
                String timeStr = minutes > 0 ? minutes + "m " + secs + "s" : secs + "s";

                server.getPlayerManager().broadcast(
                        Text.literal("All players died! " + longestName + " survived the longest: " + timeStr)
                                .formatted(Formatting.GOLD),
                        false
                );

                spawnSpectatorsNearKiller(server);
                stop(server);
                return;
            }
        }

        // FREEZE MODE WIN CHECK
        if (freezeMode) {

            List<ServerPlayerEntity> allPlayers = server.getPlayerManager().getPlayerList();

            int unfrozenRunners = 0;
            for (ServerPlayerEntity p : allPlayers) {
                if (!isFreezer(p.getUuid()) && !isFrozen(p.getUuid())) {
                    unfrozenRunners++;
                }
            }

            if (unfrozenRunners == 0 && !FROZEN.isEmpty()) {

                long longestTime = 0;
                String longestName = "Unknown";

                for (Map.Entry<UUID, Long> entry : RUNNER_START.entrySet()) {
                    UUID id = entry.getKey();
                    if (isFreezer(id)) continue;
                    long survived = System.currentTimeMillis() - entry.getValue();
                    if (survived > longestTime) {
                        longestTime = survived;
                        longestName = PLAYER_NAMES.getOrDefault(id, "Unknown");
                    }
                }

                long seconds = longestTime / 1000;
                long minutes = seconds / 60;
                long secs = seconds % 60;
                String timeStr = minutes > 0 ? minutes + "m " + secs + "s" : secs + "s";

                server.getPlayerManager().broadcast(
                        Text.literal("All players are frozen! " + longestName + " survived the longest: " + timeStr)
                                .formatted(Formatting.AQUA),
                        false
                );

                stop(server);
            }
        }
    }

    // =========================
    // SPECTATOR SPAWN NEAR KILLER
    // =========================

    private static void spawnSpectatorsNearKiller(MinecraftServer server) {

        if (lastKiller == null) return;

        ServerPlayerEntity killerPlayer = server.getPlayerManager().getPlayer(lastKiller);
        if (killerPlayer == null) return;

        ServerWorld world = killerPlayer.getServerWorld();
        BlockPos killerPos = killerPlayer.getBlockPos();

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (p.isSpectator() && !p.getUuid().equals(lastKiller)) {
                BlockPos safePos = findSafeNearbyPos(world, killerPos);
                if (safePos != null) {
                    p.teleport(world,
                            safePos.getX() + 0.5,
                            safePos.getY(),
                            safePos.getZ() + 0.5,
                            p.getYaw(),
                            p.getPitch());
                }
            }
        }
    }

    private static BlockPos findSafeNearbyPos(ServerWorld world, BlockPos center) {

        for (int attempts = 0; attempts < 100; attempts++) {

            int dist = 2 + RANDOM.nextInt(4);
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            int dx = (int) Math.round(Math.cos(angle) * dist);
            int dz = (int) Math.round(Math.sin(angle) * dist);

            BlockPos candidate = center.add(dx, 0, dz);

            for (int dy = 3; dy >= -3; dy--) {
                BlockPos feet = new BlockPos(candidate.getX(), center.getY() + dy, candidate.getZ());
                BlockPos below = feet.down();
                BlockPos head = feet.up();

                BlockState belowState = world.getBlockState(below);
                BlockState feetState = world.getBlockState(feet);
                BlockState headState = world.getBlockState(head);

                if (!belowState.isSolidBlock(world, below)) continue;
                if (belowState.isOf(Blocks.LAVA)) continue;
                if (belowState.isOf(Blocks.WATER)) continue;
                if (belowState.isOf(Blocks.POWDER_SNOW)) continue;
                if (belowState.isOf(Blocks.SAND)) continue;
                if (belowState.isOf(Blocks.GRAVEL)) continue;
                if (belowState.isOf(Blocks.RED_SAND)) continue;
                if (!feetState.isAir()) continue;
                if (!headState.isAir()) continue;

                return feet;
            }
        }

        return null;
    }

    // =========================
    // GLOWING WITH COLOURS
    // =========================

    public static void syncGlowing(MinecraftServer server) {

        Scoreboard scoreboard = server.getScoreboard();

        setupTeam(scoreboard, TEAM_CHASER,  Formatting.RED);
        setupTeam(scoreboard, TEAM_FREEZER, Formatting.AQUA);
        setupTeam(scoreboard, TEAM_KILLER,  Formatting.RED);
        setupTeam(scoreboard, TEAM_RUNNER,  Formatting.GREEN);

        for (String teamName : new String[]{TEAM_CHASER, TEAM_FREEZER, TEAM_KILLER, TEAM_RUNNER}) {
            Team t = scoreboard.getTeam(teamName);
            if (t != null) {
                new HashSet<>(t.getPlayerList()).forEach(scoreboard::clearTeam);
            }
        }

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {

            p.setGlowing(false);

            if (!gameRunning) continue;

            String playerName = p.getName().getString();

            if (killerMode) {
                if (isKiller(p.getUuid())) {
                    p.setGlowing(true);
                    scoreboard.addScoreHolderToTeam(playerName, scoreboard.getTeam(TEAM_KILLER));
                } else {
                    scoreboard.addScoreHolderToTeam(playerName, scoreboard.getTeam(TEAM_RUNNER));
                }

            } else if (freezeMode) {
                if (isFreezer(p.getUuid())) {
                    p.setGlowing(true);
                    scoreboard.addScoreHolderToTeam(playerName, scoreboard.getTeam(TEAM_FREEZER));
                } else {
                    scoreboard.addScoreHolderToTeam(playerName, scoreboard.getTeam(TEAM_RUNNER));
                }

            } else {
                if (isChaser(p.getUuid())) {
                    p.setGlowing(true);
                    scoreboard.addScoreHolderToTeam(playerName, scoreboard.getTeam(TEAM_CHASER));
                } else {
                    scoreboard.addScoreHolderToTeam(playerName, scoreboard.getTeam(TEAM_RUNNER));
                }
            }
        }
    }

    private static void setupTeam(Scoreboard scoreboard, String name, Formatting color) {
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.addTeam(name);
        }
        team.setColor(color);
        team.setShowFriendlyInvisibles(false);
    }

    // =========================
    // STOP
    // =========================

    public static void stop(MinecraftServer server) {

        for (UUID id : new HashSet<>(FROZEN)) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
            if (p != null) {
                TagIceInteractHandler.removeIceCage(p);
            }
        }

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            p.setGlowing(false);
            if (p.isSpectator()) {
                p.changeGameMode(GameMode.SURVIVAL);
            }
        }

        reset(server);

        server.getPlayerManager().broadcast(
                Text.literal("Round Ended!").formatted(Formatting.GOLD),
                false
        );
    }

    // =========================
    // SOFT RESET (clears roles but keeps players in game)
    // =========================

    public static void softReset(MinecraftServer server) {

        for (UUID id : new HashSet<>(FROZEN)) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
            if (p != null) {
                TagIceInteractHandler.removeIceCage(p);
            }
        }

        FREEZERS.clear();
        FROZEN.clear();
        FROZEN_SINCE.clear();
        TagIceInteractHandler.FREEZE_TIMERS.clear();
        RUNNER_START.clear();
        PLAYER_NAMES.clear();

        killer = null;
        chaser = null;
        gameRunning = false;

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            p.setGlowing(false);
            if (p.isSpectator()) {
                p.changeGameMode(GameMode.SURVIVAL);
            }
        }

        if (server != null) {
            Scoreboard scoreboard = server.getScoreboard();
            for (String teamName : new String[]{TEAM_CHASER, TEAM_FREEZER, TEAM_KILLER, TEAM_RUNNER}) {
                Team t = scoreboard.getTeam(teamName);
                if (t != null) {
                    new HashSet<>(t.getPlayerList()).forEach(scoreboard::clearTeam);
                }
            }
        }
    }

    // =========================
    // RESET
    // =========================

    public static void reset(MinecraftServer server) {

        gameRunning = false;
        restarting = false;

        FREEZERS.clear();
        FROZEN.clear();
        FROZEN_SINCE.clear();
        TagIceInteractHandler.FREEZE_TIMERS.clear();

        RUNNER_START.clear();
        PLAYER_NAMES.clear();

        killer = null;
        chaser = null;

        longestSurvivor = null;
        longestSurvivalTime = 0;

        roundEnding = false;

        if (server != null) {
            Scoreboard scoreboard = server.getScoreboard();
            for (String teamName : new String[]{TEAM_CHASER, TEAM_FREEZER, TEAM_KILLER, TEAM_RUNNER}) {
                Team t = scoreboard.getTeam(teamName);
                if (t != null) {
                    new HashSet<>(t.getPlayerList()).forEach(scoreboard::clearTeam);
                }
            }
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                p.setGlowing(false);
            }
        }
    }

    // =========================
    // ICE CAGE
    // =========================

    public static void buildIceCage(ServerPlayerEntity p) {

        BlockPos base = p.getBlockPos();
        var w = p.getServerWorld();

        w.setBlockState(base.down(), Blocks.BARRIER.getDefaultState());

        BlockPos[] positions = {
                base.north(),
                base.south(),
                base.east(),
                base.west(),
                base.north().up(),
                base.south().up(),
                base.east().up(),
                base.west().up(),
                base.up().up()
        };

        for (BlockPos b : positions) {
            w.setBlockState(b, Blocks.ICE.getDefaultState());
        }
    }

    // =========================
    // TITLE HELPER
    // =========================

    public static void sendTitle(ServerPlayerEntity player, Text title, Text subtitle) {
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 15, 5));
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
    }
}