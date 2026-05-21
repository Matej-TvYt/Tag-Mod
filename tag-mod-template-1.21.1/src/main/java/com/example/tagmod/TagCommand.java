package com.example.tagmod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.border.WorldBorder;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class TagCommand {

    public static void register(com.mojang.brigadier.CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(
                literal("tagstart")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {

                            TagGame.start(ctx.getSource().getServer());

                            return 1;
                        })
        );

        dispatcher.register(
                literal("tagstop")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {

                            TagGame.stop(ctx.getSource().getServer());

                            ctx.getSource().sendFeedback(
                                    () -> Text.literal("Tag game stopped.")
                                            .formatted(Formatting.RED),
                                    true
                            );

                            return 1;
                        })
        );

        dispatcher.register(
                literal("tag")
                        .requires(source -> source.hasPermissionLevel(2))

                        // =========================
                        // /tag modify worldBorder <size>
                        // =========================
                        .then(
                                literal("modify")
                                        .then(
                                                literal("worldBorder")

                                                        .then(
                                                                argument(
                                                                        "size",
                                                                        IntegerArgumentType.integer(1, 30000000)
                                                                )

                                                                        .executes(ctx -> {

                                                                            int size = IntegerArgumentType.getInteger(ctx, "size");

                                                                            ServerWorld world =
                                                                                    ctx.getSource()
                                                                                            .getServer()
                                                                                            .getOverworld();

                                                                            WorldBorder border = world.getWorldBorder();

                                                                            border.setCenter(0, 0);
                                                                            border.setSize(size * 2.0);

                                                                            ctx.getSource().sendFeedback(
                                                                                    () -> Text.literal(
                                                                                                    "World border set to "
                                                                                                            + size
                                                                                                            + " blocks."
                                                                                            )
                                                                                            .formatted(Formatting.GREEN),
                                                                                    true
                                                                            );

                                                                            return 1;
                                                                        })
                                                        )

                                                        // =========================
                                                        // /tag modify worldBorder normal
                                                        // =========================
                                                        .then(
                                                                literal("normal")
                                                                        .executes(ctx -> {

                                                                            ServerWorld world =
                                                                                    ctx.getSource()
                                                                                            .getServer()
                                                                                            .getOverworld();

                                                                            WorldBorder border = world.getWorldBorder();

                                                                            border.setCenter(0, 0);

                                                                            // Vanilla default
                                                                            border.setSize(60000000);

                                                                            ctx.getSource().sendFeedback(
                                                                                    () -> Text.literal(
                                                                                                    "World border reset to normal."
                                                                                            )
                                                                                            .formatted(Formatting.YELLOW),
                                                                                    true
                                                                            );

                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
        );
    }
}