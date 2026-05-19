package com.example.tagmod;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public class TagHitHandler {

    public static void register() {

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (world.isClient) return ActionResult.PASS;

            if (!(player instanceof ServerPlayerEntity attacker)) return ActionResult.PASS;
            if (!(entity instanceof ServerPlayerEntity target)) return ActionResult.PASS;

            if (!TagGame.gameRunning) return ActionResult.PASS;

            if (!TagGame.isChaser(attacker.getUuid())) return ActionResult.PASS;

            if (TagGame.hasProtection(target.getUuid())) return ActionResult.FAIL;

            if (attacker.getUuid().equals(target.getUuid())) return ActionResult.PASS;

            TagGame.onTagged(attacker.getServer(), target.getUuid());

            return ActionResult.SUCCESS;
        });
    }
}