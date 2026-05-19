package com.example.tagmod;

import net.fabricmc.api.ModInitializer;

public class TagMod implements ModInitializer {

    @Override
    public void onInitialize() {

        TagBorderEnforcer.register();
        TagCommand.register();
        TagHitHandler.register();
    }
}