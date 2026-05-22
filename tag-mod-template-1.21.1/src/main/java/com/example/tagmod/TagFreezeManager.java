package com.example.tagmod;

import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class TagFreezeManager {

    public static final Map<UUID, Long> frozenPlayers = new HashMap<>();

    public static void freezePlayer(ServerPlayerEntity player) {

        UUID id = player.getUuid();

        frozenPlayers.put(id, System.currentTimeMillis());

        BlockPos pos = player.getBlockPos();

        player.getServerWorld().setBlockState(pos.north(), Blocks.ICE.getDefaultState());
        player.getServerWorld().setBlockState(pos.south(), Blocks.ICE.getDefaultState());
        player.getServerWorld().setBlockState(pos.east(), Blocks.ICE.getDefaultState());
        player.getServerWorld().setBlockState(pos.west(), Blocks.ICE.getDefaultState());

        player.sendMessage(
                net.minecraft.text.Text.literal("You got Freezed, wait to get Unfreezed.")
                        .formatted(net.minecraft.util.Formatting.AQUA),
                true
        );
    }

    public static void unfreezePlayer(ServerPlayerEntity player) {

        UUID id = player.getUuid();

        frozenPlayers.remove(id);

        BlockPos pos = player.getBlockPos();

        player.getServerWorld().breakBlock(pos.north(), false);
        player.getServerWorld().breakBlock(pos.south(), false);
        player.getServerWorld().breakBlock(pos.east(), false);
        player.getServerWorld().breakBlock(pos.west(), false);

        player.sendMessage(
                net.minecraft.text.Text.literal("You got Unfreezed")
                        .formatted(net.minecraft.util.Formatting.BLUE),
                true
        );
    }

    public static boolean isFrozen(UUID id) {
        return frozenPlayers.containsKey(id);
    }
}