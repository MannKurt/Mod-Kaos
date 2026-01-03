package com.mot;

import com.mot.datagen.ModBlockTag;
import com.mot.datagen.ModLootTable;
import com.mot.datagen.ModModel;
import com.mot.datagen.ModRecipe;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModBlockTag::new);
		pack.addProvider(ModLootTable::new);
		pack.addProvider(ModModel::new);
		pack.addProvider(ModRecipe::new);
	}
}

