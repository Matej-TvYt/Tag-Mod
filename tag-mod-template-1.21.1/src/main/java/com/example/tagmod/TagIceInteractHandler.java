package com.example.tagmod;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagIceInteractHandler {

    // tracks when each player was frozen (ms)
    public static final Map<UUID, Long> FREEZE_TIMERS = new HashMap<>();

    private static final long FREEZE_DURATION_MS = 15_000; // 15 seconds

    public static void register() {

        // hitting ice/barrier block = unfreeze nearby frozen player
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {

            if (world.isClient()) return ActionResult.PASS;

            if (!(player instanceof ServerPlayerEntity sp))
                return ActionResult.PASS;

            if (!TagGame.gameRunning || !TagGame.freezeMode)
                return ActionResult.PASS;

            if (!world.getBlockState(pos).isOf(Blocks.ICE)
                    && !world.getBlockState(pos).isOf(Blocks.BARRIER))
                return ActionResult.PASS;

            // only unfrozen runners can unfreeze
            if (!TagGame.isRunner(sp.getUuid())) return ActionResult.FAIL;
            if (TagGame.isFrozen(sp.getUuid())) return ActionResult.FAIL;

            tryRunnerUnfreeze(sp, pos);

            return ActionResult.FAIL;
        });

        // tick: check 15s freeze timers + show countdown on frozen player's screen
        ServerTickEvents.END_SERVER_TICK.register(server -> {

            if (!TagGame.gameRunning || !TagGame.freezeMode) return;

            long now = System.currentTimeMillis();

            for (UUID id : TagGame.FROZEN.toArray(new UUID[0])) {

                Long frozenAt = FREEZE_TIMERS.get(id);
                if (frozenAt == null) continue;

                long elapsed = now - frozenAt;
                long remaining = (FREEZE_DURATION_MS - elapsed) / 1000;

                ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
                if (p == null) continue;

                if (elapsed >= FREEZE_DURATION_MS) {

                    // unfreeze and make them a freezer
                    TagGame.FROZEN.remove(id);
                    TagGame.FREEZERS.add(id);
                    FREEZE_TIMERS.remove(id);

                    removeIceCage(p);

                    String name = p.getName().getString();

                    server.getPlayerManager().broadcast(
                            Text.literal(name + " was frozen for too long, they became a freezer!")
                                    .formatted(Formatting.AQUA),
                            false
                    );

                    sendTitle(p,
                            Text.literal("YOU ARE NOW A FREEZER").formatted(Formatting.AQUA),
                            Text.literal("You were frozen too long!").formatted(Formatting.GRAY));

                    TagGame.syncGlowing(server);

                } else {
                    // show countdown only on the frozen player's screen (action bar)
                    p.networkHandler.sendPacket(new OverlayMessageS2CPacket(
                            Text.literal("You will become a freezer in: " + (remaining + 1) + "s")
                                    .formatted(Formatting.AQUA)
                    ));
                }
            }
        });
    }

    private static void tryRunnerUnfreeze(ServerPlayerEntity clicker, BlockPos clickedPos) {

        for (ServerPlayerEntity target :
                clicker.getServer().getPlayerManager().getPlayerList()) {

            if (!TagGame.isFrozen(target.getUuid())) continue;

            BlockPos base = target.getBlockPos();

            BlockPos[] cage = new BlockPos[]{
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

            for (BlockPos b : cage) {
                if (b.equals(clickedPos)) {

                    String targetName = target.getName().getString();
                    String clickerName = clicker.getName().getString();

                    TagGame.unfreeze(target);
                    FREEZE_TIMERS.remove(target.getUuid());
                    removeIceCage(target);

                    sendTitle(clicker,
                            Text.literal("You Unfreezed " + targetName).formatted(Formatting.GREEN),
                            Text.literal("").formatted(Formatting.GRAY));

                    sendTitle(target,
                            Text.literal("You got Unfreezed by " + clickerName).formatted(Formatting.GREEN),
                            Text.literal("Run away!").formatted(Formatting.GRAY));

                    return;
                }
            }
        }
    }

    public static void removeIceCage(ServerPlayerEntity p) {

        BlockPos base = p.getBlockPos();
        var w = p.getServerWorld();

        BlockPos[] positions = {
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

        for (BlockPos b : positions) {
            if (w.getBlockState(b).isOf(Blocks.ICE)
                    || w.getBlockState(b).isOf(Blocks.BARRIER)) {
                w.setBlockState(b, Blocks.AIR.getDefaultState());
            }
        }
    }

    private static void sendTitle(ServerPlayerEntity player, Text title, Text subtitle) {
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
    }
}