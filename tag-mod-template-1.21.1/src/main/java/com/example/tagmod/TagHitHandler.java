package com.example.tagmod;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public class TagHitHandler {

    public static void register() {

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (!(player instanceof ServerPlayerEntity attacker)) return ActionResult.PASS;
            if (!(entity instanceof ServerPlayerEntity target)) return ActionResult.PASS;

            if (!TagGame.isRunning()) return ActionResult.PASS;

            if (!TagGame.isChaser(attacker.getUuid())) return ActionResult.PASS;

            // ROLE TRANSFER HAPPENS HERE
            TagGame.transferChase(attacker.getServer(), target.getUuid());

            return ActionResult.SUCCESS;
        });
    }
}