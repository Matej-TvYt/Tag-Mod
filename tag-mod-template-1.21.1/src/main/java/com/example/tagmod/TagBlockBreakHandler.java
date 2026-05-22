package com.example.tagmod;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Blocks;

public class TagBlockBreakHandler {

    public static void register() {

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {

            // Always protect ICE blocks from real breaking
            if (state.isOf(Blocks.ICE)) {
                return false;
            }

            return true;
        });
    }
}