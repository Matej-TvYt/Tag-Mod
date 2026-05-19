package com.example.tagmod;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class TagCommand {

    public static void register() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("tagstart")
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        TagGame.start(source.getServer());
                        return 1;
                    })
            );
        });
    }
}