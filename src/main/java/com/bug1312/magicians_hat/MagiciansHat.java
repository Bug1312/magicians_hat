package com.bug1312.magicians_hat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

public class MagiciansHat implements ModInitializer {

	protected static final String MOD_ID = "magicians_hat";
	
	@Override
	public void onInitialize() {
		Register.initialize();
		
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
				.register(content -> content.addAfter(Items.TURTLE_HELMET, Register.HAT_ITEM));
	}

}