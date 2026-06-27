package com.example.tagmod;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

public class TagHitHandler {

    public static void register() {

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (world.isClient()) return ActionResult.PASS;

            if (!(player instanceof ServerPlayerEntity attacker))
                return ActionResult.PASS;

            if (!(entity instanceof ServerPlayerEntity target))
                return ActionResult.PASS;

            if (!TagGame.gameRunning)
                return ActionResult.PASS;

            // =====================
            // FREEZE MODE
            // =====================

            if (TagGame.freezeMode) {

                if (!TagGame.isFreezer(attacker.getUuid()))
                    return ActionResult.PASS;

                if (TagGame.isFreezer(target.getUuid()))
                    return ActionResult.FAIL;

                if (TagGame.isFrozen(target.getUuid()))
                    return ActionResult.FAIL;

                TagGame.freeze(target);

                sendTitle(target,
                        Text.literal("YOU GOT FROZEN").formatted(Formatting.AQUA),
                        Text.literal("Wait to be unfrozen!").formatted(Formatting.GRAY));

                sendTitle(attacker,
                        Text.literal("FROZEN!").formatted(Formatting.AQUA),
                        Text.literal("You froze " + target.getName().getString()).formatted(Formatting.GRAY));

                return ActionResult.SUCCESS;
            }

            // =====================
            // KILLER MODE
            // =====================

            if (TagGame.killerMode) {

                if (!TagGame.isKiller(attacker.getUuid()))
                    return ActionResult.PASS;

                if (target.isSpectator())
                    return ActionResult.FAIL;

                target.changeGameMode(net.minecraft.world.GameMode.SPECTATOR);

                sendTitle(target,
                        Text.literal("YOU DIED").formatted(Formatting.RED),
                        Text.literal("You were killed!").formatted(Formatting.GRAY));

                sendTitle(attacker,
                        Text.literal("KILL!").formatted(Formatting.RED),
                        Text.literal("You killed " + target.getName().getString()).formatted(Formatting.GRAY));

                return ActionResult.SUCCESS;
            }

            // =====================
            // NORMAL TAG MODE
            // =====================

            if (!TagGame.isChaser(attacker.getUuid()))
                return ActionResult.PASS;

            TagGame.transferChase(attacker.getServer(), target.getUuid());

            sendTitle(target,
                    Text.literal("YOU ARE THE CHASER").formatted(Formatting.RED),
                    Text.literal("Chase someone!").formatted(Formatting.GRAY));

            sendTitle(attacker,
                    Text.literal("YOU ARE A RUNNER").formatted(Formatting.GREEN),
                    Text.literal("Run away!").formatted(Formatting.GRAY));

            return ActionResult.SUCCESS;
        });
    }

    // 0.75 seconds = ~15 ticks stay time
    public static void sendTitle(ServerPlayerEntity player, Text title, Text subtitle) {
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 15, 5));
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
    }
}