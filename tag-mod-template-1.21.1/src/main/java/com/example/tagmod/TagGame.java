package com.example.tagmod;

import net.minecraft.block.Blocks;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class TagGame {

    // =========================
    // GAME STATE
    // =========================

    public static boolean roundEnding = false;
    public static long roundEndingStart = 0;
    public static boolean gameRunning = false;
    public static boolean freezeMode = false;

    // normal mode
    public static UUID chaser = null;

    // freeze mode
    public static final Set<UUID> FREEZERS = new HashSet<>();
    public static final Set<UUID> FROZEN = new HashSet<>();
    public static final Map<UUID, Long> FROZEN_SINCE = new HashMap<>();

    // runner tracking
    public static final Map<UUID, Long> RUNNER_START = new HashMap<>();
    public static final Map<UUID, String> PLAYER_NAMES = new HashMap<>();

    public static UUID longestSurvivor = null;
    public static long longestSurvivalTime = 0;

    public static boolean endingRound = false;

    // HUD/actionbar
    private static final Map<UUID, Integer> ACTIONBAR_TICKS = new HashMap<>();
    private static final Map<UUID, Text> ACTIONBAR_TEXT = new HashMap<>();

    private static final Random RANDOM = new Random();

    // =========================
    // STATE CHECKS
    // =========================

    public static boolean isRunning() {
        return gameRunning;
    }

    public static boolean isChaser(UUID id) {
        return chaser != null && chaser.equals(id);
    }

    public static boolean isFreezer(UUID id) {
        return FREEZERS.contains(id);
    }

    public static boolean isFrozen(UUID id) {
        return FROZEN.contains(id);
    }

    public static boolean isRunner(UUID id) {
        return !FREEZERS.contains(id);
    }

    // =========================
    // START
    // =========================

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

        if (freezeMode && players.size() < 3) {

            server.getPlayerManager().broadcast(
                    Text.literal("Need at least 3 players for Freeze Mode.")
                            .formatted(Formatting.RED),
                    false
            );

            return;
        }

        resetRound(server);

        gameRunning = true;

        RUNNER_START.clear();
        PLAYER_NAMES.clear();

        longestSurvivor = null;
        longestSurvivalTime = 0;

        for (ServerPlayerEntity p : players) {

            PLAYER_NAMES.put(
                    p.getUuid(),
                    p.getName().getString()
            );
        }

        ServerPlayerEntity selected =
                players.get(RANDOM.nextInt(players.size()));

        // =========================
        // FREEZE MODE
        // =========================

        if (freezeMode) {

            FREEZERS.add(selected.getUuid());

            for (ServerPlayerEntity p : players) {

                if (!p.getUuid().equals(selected.getUuid())) {

                    RUNNER_START.put(
                            p.getUuid(),
                            System.currentTimeMillis()
                    );
                }
            }

            server.getPlayerManager().broadcast(
                    Text.literal(
                            "Tag game started, "
                                    + selected.getName().getString()
                                    + " Is the FREEZER!"
                    ).formatted(Formatting.AQUA),
                    false
            );

            for (ServerPlayerEntity p : players) {

                if (isFreezer(p.getUuid())) {

                    showActionbar(
                            p,
                            Text.literal("You became a Freezer")
                                    .formatted(Formatting.AQUA)
                    );

                } else {

                    showActionbar(
                            p,
                            Text.literal("You became a Runner")
                                    .formatted(Formatting.GREEN)
                    );
                }
            }

        } else {

            // =========================
            // NORMAL MODE
            // =========================

            chaser = selected.getUuid();

            for (ServerPlayerEntity p : players) {

                if (!p.getUuid().equals(selected.getUuid())) {

                    RUNNER_START.put(
                            p.getUuid(),
                            System.currentTimeMillis()
                    );
                }
            }

            server.getPlayerManager().broadcast(
                    Text.literal(
                            "Tag game started, "
                                    + selected.getName().getString()
                                    + " Is the CHASER!"
                    ).formatted(Formatting.RED),
                    false
            );

            for (ServerPlayerEntity p : players) {

                if (isChaser(p.getUuid())) {

                    showActionbar(
                            p,
                            Text.literal("You became the Chaser")
                                    .formatted(Formatting.RED)
                    );

                } else {

                    showActionbar(
                            p,
                            Text.literal("You became a Runner")
                                    .formatted(Formatting.GREEN)
                    );
                }
            }
        }

        syncAll(server);
    }

    // =========================
    // STOP
    // =========================

    public static void stop(MinecraftServer server) {

        gameRunning = false;

        for (ServerPlayerEntity p :
                server.getPlayerManager().getPlayerList()) {

            roundEnding = false;
            roundEndingStart = 0;
            p.setGlowing(false);
            p.setNoGravity(false);

            if (FROZEN.contains(p.getUuid())) {
                removeIceCage(p);
            }
        }

        chaser = null;

        FREEZERS.clear();
        FROZEN.clear();
        FROZEN_SINCE.clear();

        RUNNER_START.clear();
        PLAYER_NAMES.clear();

        longestSurvivor = null;
        longestSurvivalTime = 0;

        endingRound = false;

        ACTIONBAR_TICKS.clear();
        ACTIONBAR_TEXT.clear();

        clearTeams(server);

        server.getPlayerManager().broadcast(
                Text.literal("Tag game stopped.")
                        .formatted(Formatting.RED),
                false
        );
    }

    // =========================
    // RESET ROUND
    // =========================

    private static void resetRound(MinecraftServer server) {

        for (ServerPlayerEntity p :
                server.getPlayerManager().getPlayerList()) {

            roundEnding = false;
            roundEndingStart = 0;
            p.setGlowing(false);
            p.setNoGravity(false);

            if (FROZEN.contains(p.getUuid())) {
                removeIceCage(p);
            }
        }

        chaser = null;

        FREEZERS.clear();
        FROZEN.clear();
        FROZEN_SINCE.clear();

        RUNNER_START.clear();
        PLAYER_NAMES.clear();

        longestSurvivor = null;
        longestSurvivalTime = 0;

        endingRound = false;

        ACTIONBAR_TICKS.clear();
        ACTIONBAR_TEXT.clear();

        clearTeams(server);
    }

    // =========================
    // NORMAL TAG TRANSFER
    // =========================

    public static void transferChase(
            MinecraftServer server,
            UUID newChaser
    ) {

        if (!gameRunning) return;
        if (freezeMode) return;

        chaser = newChaser;

        for (ServerPlayerEntity p :
                server.getPlayerManager().getPlayerList()) {

            if (isChaser(p.getUuid())) {

                showActionbar(
                        p,
                        Text.literal("You became the Chaser")
                                .formatted(Formatting.RED)
                );

            } else {

                showActionbar(
                        p,
                        Text.literal("You became a Runner")
                                .formatted(Formatting.GREEN)
                );
            }
        }

        syncAll(server);
    }

    // =========================
    // FREEZE PLAYER
    // =========================

    public static void freeze(ServerPlayerEntity target) {

        if (!gameRunning || !freezeMode)
            return;

        UUID id = target.getUuid();

        if (FREEZERS.contains(id))
            return;

        if (FROZEN.contains(id))
            return;

        // save survival result ONCE
        if (RUNNER_START.containsKey(id)) {

            long survived =
                    System.currentTimeMillis()
                            - RUNNER_START.get(id);

            if (survived > longestSurvivalTime) {

                longestSurvivalTime = survived;
                longestSurvivor = id;
            }
        }

        FROZEN.add(id);

        FROZEN_SINCE.put(
                id,
                System.currentTimeMillis()
        );

        buildIceCage(target);

        showActionbar(
                target,
                Text.literal(
                        "You got Freezed, wait to get Unfreezed."
                ).formatted(Formatting.AQUA)
        );
    }

    // =========================
    // UNFREEZE PLAYER
    // =========================

    public static void unfreeze(ServerPlayerEntity target) {

        UUID id = target.getUuid();

        if (!FROZEN.contains(id))
            return;

        FROZEN.remove(id);
        FROZEN_SINCE.remove(id);

        removeIceCage(target);

        target.setNoGravity(false);

        showActionbar(
                target,
                Text.literal("You got Unfreezed.")
                        .formatted(Formatting.BLUE)
        );
    }

    // =========================
    // TICK
    // =========================

    public static void tick(MinecraftServer server) {

        if (!gameRunning)
            return;

        // actionbar
        for (ServerPlayerEntity p :
                server.getPlayerManager().getPlayerList()) {

            UUID id = p.getUuid();

            Integer ticks =
                    ACTIONBAR_TICKS.get(id);

            if (ticks == null)
                continue;

            if (ticks > 0) {

                p.sendMessage(
                        ACTIONBAR_TEXT.get(id),
                        true
                );

                ACTIONBAR_TICKS.put(id, ticks - 1);

            } else {

                ACTIONBAR_TICKS.remove(id);
                ACTIONBAR_TEXT.remove(id);
            }
        }

        if (!freezeMode)
            return;

        List<UUID> convert = new ArrayList<>();

        for (ServerPlayerEntity p :
                server.getPlayerManager().getPlayerList()) {

            UUID id = p.getUuid();

            if (!FROZEN.contains(id)) {

                p.setNoGravity(false);

                continue;
            }

            // hard freeze
            p.setVelocity(0, 0, 0);
            p.velocityModified = true;

            p.setNoGravity(true);

            p.teleport(
                    p.getServerWorld(),
                    p.getX(),
                    p.getY(),
                    p.getZ(),
                    p.getYaw(),
                    p.getPitch()
            );

            long elapsed =
                    (System.currentTimeMillis()
                            - FROZEN_SINCE.get(id)) / 1000;

            long remain =
                    Math.max(0, 60 - elapsed);

            p.sendMessage(
                    Text.literal(
                            remain
                                    + "s Until you become a Freezer."
                    ).formatted(Formatting.AQUA),
                    true
            );

            if (elapsed >= 60) {
                convert.add(id);
            }
        }

        // convert frozen -> freezer
        for (UUID id : convert) {

            ServerPlayerEntity p =
                    server.getPlayerManager().getPlayer(id);

            if (p == null)
                continue;

            removeIceCage(p);

            FROZEN.remove(id);
            FROZEN_SINCE.remove(id);

            FREEZERS.add(id);

            p.setNoGravity(false);

            showActionbar(
                    p,
                    Text.literal("You became a Freezer")
                            .formatted(Formatting.AQUA)
            );
        }

        syncAll(server);

        checkForFreezeWin(server);
    }

    // =========================
    // ROUND END
    // =========================

    public static void checkForFreezeWin(
            MinecraftServer server
    ) {

        if (!freezeMode || !gameRunning)
            return;

        int aliveRunners = 0;

        // count NON-FROZEN runners
        for (ServerPlayerEntity p :
                server.getPlayerManager().getPlayerList()) {

            UUID id = p.getUuid();

            if (isRunner(id)
                    && !isFrozen(id)) {

                aliveRunners++;
            }
        }

        // at least one runner still alive
        if (aliveRunners > 0) {

            roundEnding = false;

            return;
        }

        // START END COUNTDOWN
        if (!roundEnding) {

            roundEnding = true;

            roundEndingStart =
                    System.currentTimeMillis();
        }

        long elapsed =
                (System.currentTimeMillis()
                        - roundEndingStart) / 1000;

        long remain =
                Math.max(0, 10 - elapsed);

        // HUD FOR ALL PLAYERS
        for (ServerPlayerEntity p :
                server.getPlayerManager().getPlayerList()) {

            p.sendMessage(
                    Text.literal(
                            remain +
                                    " Until round ends."
                    ).formatted(Formatting.GOLD),
                    true
            );
        }

        // still counting
        if (elapsed < 10)
            return;

        roundEnding = false;

        String winnerName = "Unknown";

        if (longestSurvivor != null
                && PLAYER_NAMES.containsKey(longestSurvivor)) {

            winnerName =
                    PLAYER_NAMES.get(longestSurvivor);
        }

        long totalSeconds =
                longestSurvivalTime / 1000;

        long minutes =
                totalSeconds / 60;

        long seconds =
                totalSeconds % 60;

        // REMOVE ALL ICE
        for (ServerPlayerEntity p :
                server.getPlayerManager().getPlayerList()) {

            if (isFrozen(p.getUuid())) {

                removeIceCage(p);

                p.setNoGravity(false);
            }
        }

        FROZEN.clear();
        FROZEN_SINCE.clear();

        // SHOW STATS
        server.getPlayerManager().broadcast(
                Text.literal(
                        winnerName
                                + " survived the longest: "
                                + minutes + "m "
                                + seconds + "s"
                ).formatted(Formatting.GOLD),
                false
        );

        stop(server);
    }

    // =========================
    // HUD
    // =========================

    private static void showActionbar(
            ServerPlayerEntity player,
            Text text
    ) {

        ACTIONBAR_TEXT.put(
                player.getUuid(),
                text
        );

        ACTIONBAR_TICKS.put(
                player.getUuid(),
                40
        );

        player.sendMessage(text, true);
    }

    // =========================
    // GLOWING TEAMS
    // =========================

    public static void syncAll(MinecraftServer server) {

        try {

            Scoreboard scoreboard =
                    server.getScoreboard();

            Team freezerTeam =
                    scoreboard.getTeam("tag_freezer");

            if (freezerTeam == null) {

                freezerTeam =
                        scoreboard.addTeam("tag_freezer");

                freezerTeam.setColor(Formatting.AQUA);
            }

            Team chaserTeam =
                    scoreboard.getTeam("tag_chaser");

            if (chaserTeam == null) {

                chaserTeam =
                        scoreboard.addTeam("tag_chaser");

                chaserTeam.setColor(Formatting.RED);
            }

            for (ServerPlayerEntity p :
                    server.getPlayerManager().getPlayerList()) {

                String entry =
                        p.getNameForScoreboard();

                if (freezerTeam.getPlayerList().contains(entry)) {
                    scoreboard.removeScoreHolderFromTeam(
                            entry,
                            freezerTeam
                    );
                }

                if (chaserTeam.getPlayerList().contains(entry)) {
                    scoreboard.removeScoreHolderFromTeam(
                            entry,
                            chaserTeam
                    );
                }

                p.setGlowing(false);

                if (!gameRunning)
                    continue;

                if (freezeMode) {

                    if (isFreezer(p.getUuid())) {

                        scoreboard.addScoreHolderToTeam(
                                entry,
                                freezerTeam
                        );

                        p.setGlowing(true);
                    }

                } else {

                    if (isChaser(p.getUuid())) {

                        scoreboard.addScoreHolderToTeam(
                                entry,
                                chaserTeam
                        );

                        p.setGlowing(true);
                    }
                }
            }

        } catch (Throwable t) {

            t.printStackTrace();
        }
    }

    private static void clearTeams(
            MinecraftServer server
    ) {

        try {

            Scoreboard scoreboard =
                    server.getScoreboard();

            Team freezerTeam =
                    scoreboard.getTeam("tag_freezer");

            if (freezerTeam != null) {

                for (String entry :
                        new HashSet<>(freezerTeam.getPlayerList())) {

                    scoreboard.removeScoreHolderFromTeam(
                            entry,
                            freezerTeam
                    );
                }
            }

            Team chaserTeam =
                    scoreboard.getTeam("tag_chaser");

            if (chaserTeam != null) {

                for (String entry :
                        new HashSet<>(chaserTeam.getPlayerList())) {

                    scoreboard.removeScoreHolderFromTeam(
                            entry,
                            chaserTeam
                    );
                }
            }

        } catch (Throwable t) {

            t.printStackTrace();
        }
    }

    // =========================
    // ICE CAGE
    // =========================

    private static void buildIceCage(
            ServerPlayerEntity p
    ) {

        BlockPos base = p.getBlockPos();

        var w = p.getServerWorld();

        BlockPos[] cage = new BlockPos[] {

                base.down(),

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

        for (BlockPos pos : cage) {

            if (w.isAir(pos)) {

                w.setBlockState(
                        pos,
                        Blocks.ICE.getDefaultState()
                );
            }
        }
    }

    private static void removeIceCage(
            ServerPlayerEntity p
    ) {

        BlockPos base = p.getBlockPos();

        var w = p.getServerWorld();

        BlockPos[] cage = new BlockPos[] {

                base.down(),

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

        for (BlockPos pos : cage) {

            if (w.getBlockState(pos).isOf(Blocks.ICE)) {

                w.breakBlock(pos, false);
            }
        }
    }
}