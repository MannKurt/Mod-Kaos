package com.mot.block;

import com.mot.Mod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlock {
    public static final Block BLOK1 = registerBlock("blok1",
            AbstractBlock.Settings.create().strength(4f).requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK));

    public static final Block BLOK2 = registerBlock("blok2",
            new StairsBlock(BLOK1.getDefaultState(),
                    AbstractBlock.Settings.copy(BLOK1)
                            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Mod.MOD_ID, "blok2")))));



    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(Mod.MOD_ID, name), block);
    }

    private static Block registerBlock(String name, AbstractBlock.Settings settings) {
        Identifier identifier = Identifier.of(Mod.MOD_ID, name);
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, identifier);
        AbstractBlock.Settings blockSettings = settings.registryKey(key);

        Block block = new Block(blockSettings);
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, identifier, block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(Mod.MOD_ID, name),
                new BlockItem(block, new Item.Settings()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Mod.MOD_ID, name)))));
    }

    public static void registerModBlocks() {
        Mod.LOGGER.info("Registering Mod Blocks for " + Mod.MOD_ID);
    }
}
