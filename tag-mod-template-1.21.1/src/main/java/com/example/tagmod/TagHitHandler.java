package com.example.tagmod;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public class TagHitHandler {

    public static void register() {

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            if (!(player instanceof ServerPlayerEntity attacker)) return ActionResult.PASS;
            if (!(entity instanceof ServerPlayerEntity target)) return ActionResult.PASS;

            if (!TagGame.gameRunning) return ActionResult.PASS;

            // ---- FREEZE MODE ----
            if (TagGame.freezeMode) {

                // only freezers can freeze
                if (!TagGame.isFreezer(attacker.getUuid())) return ActionResult.PASS;

                // can't freeze self, can't freeze freezers, can't freeze already frozen
                if (attacker.getUuid().equals(target.getUuid())) return ActionResult.FAIL;
                if (TagGame.isFreezer(target.getUuid())) return ActionResult.FAIL;
                if (TagGame.isFrozen(target.getUuid())) return ActionResult.FAIL;

                TagGame.freeze(target);
                return ActionResult.SUCCESS;
            }

            // ---- NORMAL MODE ----
            if (!TagGame.isChaser(attacker.getUuid())) return ActionResult.PASS;
            if (TagGame.isChaser(target.getUuid())) return ActionResult.FAIL;

            TagGame.transferChase(attacker.getServer(), target.getUuid());
            return ActionResult.SUCCESS;
        });
    }
}