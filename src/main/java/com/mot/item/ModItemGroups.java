package com.mot.item;

import com.mot.Mod;
import com.mot.block.ModBlock;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup MOD_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Mod.MOD_ID, "mod_group"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemgroup.mod"))
                    .icon(() -> new ItemStack(ModItem.KAOS_ASASI))
                    .entries((displayContext, entries) -> {

                        entries.add(ModItem.KAOS_ASASI);
                        entries.add(ModItem.LAZER_ASASI);
                        entries.add(ModItem.LOKUM);
                        entries.add(ModItem.MOD_SWORD);
                        entries.add(ModItem.MOD_PICKAXE);
                        entries.add(ModItem.MOD_AXE);
                        entries.add(ModBlock.BLOK2);
                        entries.add(ModBlock.BLOK1);

                    }).build());

    public static void registerItemGroups() {
        Mod.LOGGER.info("Registering Item Groups for " + Mod.MOD_ID);
    }
}
