package com.example.tagmod;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class TagIceInteractHandler {

    public static void register() {

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {

            if (world.isClient()) return ActionResult.PASS;

            if (!(player instanceof ServerPlayerEntity sp))
                return ActionResult.PASS;

            if (!TagGame.gameRunning || !TagGame.freezeMode)
                return ActionResult.PASS;

            if (world.getBlockState(pos).isOf(Blocks.ICE)) {

                // BLOCK ALL NORMAL BREAKING
                tryRunnerUnfreeze(sp, pos);

                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {

            if (world.isClient()) return ActionResult.PASS;

            if (!(player instanceof ServerPlayerEntity sp))
                return ActionResult.PASS;

            if (!TagGame.gameRunning || !TagGame.freezeMode)
                return ActionResult.PASS;

            if (!(hit instanceof BlockHitResult bhr))
                return ActionResult.PASS;

            BlockPos pos = bhr.getBlockPos();

            if (world.getBlockState(pos).isOf(Blocks.ICE)) {

                tryRunnerUnfreeze(sp, pos);

                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }

    private static void tryRunnerUnfreeze(ServerPlayerEntity clicker, BlockPos icePos) {

        // ONLY RUNNERS CAN UNFREEZE
        if (!TagGame.isRunner(clicker.getUuid()))
            return;

        // FROZEN PLAYERS CANNOT UNFREEZE
        if (TagGame.isFrozen(clicker.getUuid()))
            return;

        for (ServerPlayerEntity target :
                clicker.getServer().getPlayerManager().getPlayerList()) {

            if (!TagGame.isFrozen(target.getUuid()))
                continue;

            BlockPos base = target.getBlockPos();

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

            for (BlockPos p : cage) {

                if (p.equals(icePos)) {

                    TagGame.unfreeze(target);

                    return;
                }
            }
        }
    }
}