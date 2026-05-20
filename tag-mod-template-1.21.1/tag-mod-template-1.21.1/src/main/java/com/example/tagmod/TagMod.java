package com.example.tagmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class TagMod implements ModInitializer {

    @Override
    public void onInitialize() {

        System.out.println("TAG MOD LOADED");

        // commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TagCommand.register(dispatcher);
        });

        // IMPORTANT:
        // ONLY register what actually exists
        TagHitHandler.register();
    }
}