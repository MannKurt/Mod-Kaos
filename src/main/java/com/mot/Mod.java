package com.mot;

import com.mot.block.ModBlock;
import com.mot.item.ModItem;
import com.mot.item.ModItemGroups;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mod implements ModInitializer {
	public static final String MOD_ID = "mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItem.registerModItem();
		ModItemGroups.registerItemGroups();
		ModBlock.registerModBlocks();

		LOGGER.info("Hello world! AnanıSkm Dünya hayatım bok gibi sürekli ölümle mücadele ediyorym.");
	}
}