package com.mot.datagen;

import com.mot.block.ModBlock;
import com.mot.item.ModItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLootTable extends FabricBlockLootTableProvider {

    public ModLootTable(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        // Normal bloklar (Kırınca kendisi düşenler)
        addDrop(ModBlock.BLOK1);
        addDrop(ModBlock.BLOK2);

        // Özel Blok (Fortune ve Silk Touch mantığı)
        addDrop(ModBlock.BLOK1, customOreDrops(ModBlock.BLOK1, ModItem.LOKUM.asItem(), 3.0f, 7.0f));
    }

    // --- YARDIMCI METOT ---
    public LootTable.Builder customOreDrops(Block blok, Item dusenEsya, float min, float max) {

        // DÜZELTME: "this.registryLookup" yerine "this.registries" kullanıldı.
        RegistryWrapper.Impl<Enchantment> registry = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);

        return this.dropsWithSilkTouch(
                blok,
                (LootPoolEntry.Builder) this.applyExplosionDecay(
                        blok,
                        ItemEntry.builder(dusenEsya)
                                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max)))
                                .apply(ApplyBonusLootFunction.oreDrops(registry.getOrThrow(Enchantments.FORTUNE)))
                )
        );
    }
}