package com.example.tagmod;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;

public class TagBlockBreakHandler {

    public static void register() {

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {

            if (world.isClient()) return true;

            if (!(player instanceof ServerPlayerEntity sp)) return true;

            // protect ice and barrier during freeze mode
            if (state.isOf(Blocks.ICE) || state.isOf(Blocks.BARRIER)) {

                if (!TagGame.gameRunning || !TagGame.freezeMode) return false;

                // only runners who are not frozen can break it
                if (TagGame.isRunner(sp.getUuid()) && !TagGame.isFrozen(sp.getUuid())) {
                    // allow — world.breakBlock handles instant break via TagIceInteractHandler
                    return false; // still block normal break, interact handler does it
                }

                return false;
            }

            return true;
        });
    }
}