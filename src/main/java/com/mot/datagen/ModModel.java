package com.mot.datagen;

import com.mot.block.ModBlock;
import com.mot.item.ModItem;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;

public class ModModel extends FabricModelProvider {
    public ModModel(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // 1. Önce Ana Bloğun (BLOK1) doku havuzunu (Texture Pool) oluşturuyoruz.
        // Bu şu demek: "BLOK1'in texture'ını al, hafızada tut."
        BlockStateModelGenerator.BlockTexturePool texturePool = blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlock.BLOK1);
        // 2. Sonra bu havuzu kullanarak Merdiveni (BLOK2) oluşturuyoruz.
        // Bu şu demek: "BLOK1'in resmini kullanarak BLOK2 için merdiven modellerini üret."
        texturePool.stairs(ModBlock.BLOK2);

        // İpucu: İleride Yarım Blok (Slab) eklersen altına şunu da ekleyebilirsin:
        // texturePool.slab(ManniacBlocks.BLOK3);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItem.LOKUM, Models.GENERATED);
        itemModelGenerator.register(ModItem.KAOS_ASASI, Models.HANDHELD);
        itemModelGenerator.register(ModItem.MOD_AXE, Models.HANDHELD);
        itemModelGenerator.register(ModItem.MOD_PICKAXE, Models.HANDHELD);
        itemModelGenerator.register(ModItem.MOD_SWORD, Models.HANDHELD);

    }
}
