package com.example.tagmod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TagCommand {

    public static void register(com.mojang.brigadier.CommandDispatcher<ServerCommandSource> dispatcher) {

        // /tagstart
        dispatcher.register(
                CommandManager.literal("tagstart")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {
                            TagGame.start(ctx.getSource().getServer());
                            return 1;
                        })
        );

        // /tagstop
        dispatcher.register(
                CommandManager.literal("tagstop")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {
                            TagGame.stop(ctx.getSource().getServer());
                            return 1;
                        })
        );

        // /tag config ...
        dispatcher.register(
                CommandManager.literal("tag")
                        .requires(source -> source.hasPermissionLevel(2))

                        // freezeMode enable
                        .then(CommandManager.literal("config")
                                .then(CommandManager.literal("freezeMode")

                                        .then(CommandManager.literal("enable")
                                                .executes(ctx -> {

                                                    if (ctx.getSource().getServer()
                                                            .getPlayerManager()
                                                            .getPlayerList()
                                                            .size() < 3) {

                                                        ctx.getSource().sendError(
                                                                Text.literal("Need at least 3 players for Freeze Mode.")
                                                                        .formatted(Formatting.RED)
                                                        );
                                                        return 0;
                                                    }

                                                    TagGame.freezeMode = true;

                                                    ctx.getSource().sendFeedback(
                                                            () -> Text.literal("Freeze Mode enabled.")
                                                                    .formatted(Formatting.AQUA),
                                                            false
                                                    );

                                                    return 1;
                                                }))

                                        .then(CommandManager.literal("disable")
                                                .executes(ctx -> {

                                                    TagGame.freezeMode = false;

                                                    ctx.getSource().sendFeedback(
                                                            () -> Text.literal("Freeze Mode disabled.")
                                                                    .formatted(Formatting.RED),
                                                            false
                                                    );

                                                    return 1;
                                                }))
                                )

                                // worldBorder
                                .then(CommandManager.literal("worldBorder")

                                        .then(CommandManager.literal("normal")
                                                .executes(ctx -> {

                                                    ctx.getSource().getServer()
                                                            .getOverworld()
                                                            .getWorldBorder()
                                                            .setSize(60000000);

                                                    ctx.getSource().sendFeedback(
                                                            () -> Text.literal("World border reset.")
                                                                    .formatted(Formatting.GREEN),
                                                            false
                                                    );

                                                    return 1;
                                                }))

                                        .then(CommandManager.argument(
                                                        "size",
                                                        IntegerArgumentType.integer(1, 30000000)
                                                )
                                                .executes(ctx -> {

                                                    int size = IntegerArgumentType.getInteger(ctx, "size");

                                                    ctx.getSource().getServer()
                                                            .getOverworld()
                                                            .getWorldBorder()
                                                            .setSize(size * 2.0);

                                                    ctx.getSource().sendFeedback(
                                                            () -> Text.literal("World border set to " + size)
                                                                    .formatted(Formatting.GREEN),
                                                            false
                                                    );

                                                    return 1;
                                                }))
                                )
                        )
        );
    }
}