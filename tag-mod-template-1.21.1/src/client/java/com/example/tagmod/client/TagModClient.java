package com.example.tagmod.client;

import net.fabricmc.api.ClientModInitializer;

public class TagModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		TagPopup.register();
		TagEffectsClient.register();
	}
}