package com.example.tagmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class TagMod implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("TAG MOD LOADED");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TagCommand.register(dispatcher);
        });

        TagTickHandler.register();
        TagHitHandler.register();
        TagBlockBreakHandler.register();
        TagIceInteractHandler.register();
    }
}