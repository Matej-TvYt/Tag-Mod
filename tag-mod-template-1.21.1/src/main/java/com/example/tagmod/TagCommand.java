package com.example.tagmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TagCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        // /tagstart
        dispatcher.register(
                CommandManager.literal("tagstart")
                        .requires(s -> s.hasPermissionLevel(2))
                        .executes(ctx -> {

                            if (TagGame.gameRunning) {
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("A round is already running.").formatted(Formatting.RED),
                                        false
                                );
                                return 0;
                            }

                            String name = ctx.getSource().getName();
                            TagGame.start(ctx.getSource().getServer());

                            ctx.getSource().getServer().getPlayerManager().broadcast(
                                    Text.literal(name + " started the round.").formatted(Formatting.GREEN),
                                    false
                            );

                            return 1;
                        })
        );

        // /tagstop
        dispatcher.register(
                CommandManager.literal("tagstop")
                        .requires(s -> s.hasPermissionLevel(2))
                        .executes(ctx -> {

                            if (!TagGame.gameRunning) {
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("No round is currently running.").formatted(Formatting.RED),
                                        false
                                );
                                return 0;
                            }

                            String name = ctx.getSource().getName();
                            TagGame.stop(ctx.getSource().getServer());

                            ctx.getSource().getServer().getPlayerManager().broadcast(
                                    Text.literal(name + " ended the round.").formatted(Formatting.GOLD),
                                    false
                            );

                            return 1;
                        })
        );

        // /tag config ...
        dispatcher.register(
                CommandManager.literal("tag")
                        .requires(s -> s.hasPermissionLevel(2))

                        .then(CommandManager.literal("config")

                                // /tag config normalMode
                                .then(CommandManager.literal("normalMode")
                                        .executes(ctx -> {

                                            String name = ctx.getSource().getName();

                                            if (TagGame.freezeMode) {
                                                TagGame.freezeMode = false;
                                                ctx.getSource().getServer().getPlayerManager().broadcast(
                                                        Text.literal(name + " disabled Freeze Mode.").formatted(Formatting.YELLOW),
                                                        false
                                                );
                                            }

                                            if (TagGame.killerMode) {
                                                TagGame.killerMode = false;
                                                ctx.getSource().getServer().getPlayerManager().broadcast(
                                                        Text.literal(name + " disabled Killer Mode.").formatted(Formatting.YELLOW),
                                                        false
                                                );
                                            }

                                            ctx.getSource().getServer().getPlayerManager().broadcast(
                                                    Text.literal(name + " enabled Normal Mode.").formatted(Formatting.GREEN),
                                                    false
                                            );

                                            return 1;
                                        })
                                )

                                // /tag config freezeMode
                                .then(CommandManager.literal("freezeMode")
                                        .executes(ctx -> {

                                            String name = ctx.getSource().getName();

                                            if (TagGame.freezeMode) {
                                                TagGame.freezeMode = false;
                                                ctx.getSource().getServer().getPlayerManager().broadcast(
                                                        Text.literal(name + " disabled Freeze Mode.").formatted(Formatting.YELLOW),
                                                        false
                                                );
                                            } else {
                                                if (TagGame.killerMode) {
                                                    TagGame.killerMode = false;
                                                    ctx.getSource().getServer().getPlayerManager().broadcast(
                                                            Text.literal(name + " disabled Killer Mode.").formatted(Formatting.YELLOW),
                                                            false
                                                    );
                                                }
                                                TagGame.freezeMode = true;
                                                ctx.getSource().getServer().getPlayerManager().broadcast(
                                                        Text.literal(name + " enabled Freeze Mode.").formatted(Formatting.AQUA),
                                                        false
                                                );
                                            }

                                            return 1;
                                        })
                                )

                                // /tag config killerMode
                                .then(CommandManager.literal("killerMode")
                                        .executes(ctx -> {

                                            String name = ctx.getSource().getName();

                                            if (TagGame.killerMode) {
                                                TagGame.killerMode = false;
                                                ctx.getSource().getServer().getPlayerManager().broadcast(
                                                        Text.literal(name + " disabled Killer Mode.").formatted(Formatting.YELLOW),
                                                        false
                                                );
                                            } else {
                                                if (TagGame.freezeMode) {
                                                    TagGame.freezeMode = false;
                                                    ctx.getSource().getServer().getPlayerManager().broadcast(
                                                            Text.literal(name + " disabled Freeze Mode.").formatted(Formatting.YELLOW),
                                                            false
                                                    );
                                                }
                                                TagGame.killerMode = true;
                                                ctx.getSource().getServer().getPlayerManager().broadcast(
                                                        Text.literal(name + " enabled Killer Mode.").formatted(Formatting.RED),
                                                        false
                                                );
                                            }

                                            return 1;
                                        })
                                )
                        )
        );
    }
}