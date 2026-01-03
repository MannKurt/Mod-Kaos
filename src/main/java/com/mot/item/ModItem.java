package com.mot.item;

import com.mot.Mod;
import com.mot.item.custom.KaosAsasi;
import com.mot.item.custom.ModToolMaterial;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;


public class ModItem {

    public static final Item KAOS_ASASI = registerItem("kaos_asasi",
            new KaosAsasi(new Item.Settings()
                    .maxDamage(750)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Mod.MOD_ID,"kaos_asasi")))));

    public static final Item LOKUM = registerItem("lokum",
            new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Mod.MOD_ID,"lokum")))
                    .food(new FoodComponent.Builder()
                            .nutrition(2)
                            .saturationModifier(0.25F)
                            .build())
                    .component(DataComponentTypes.CONSUMABLE, ConsumableComponents.food()
                            .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.SPEED, 50, 1), 1.0f))
                            .build())
            ));
    public static final Item MOD_SWORD = registerItem("mod_sword",
            new Item(
                    ModToolMaterial.MANNIAC.applySwordSettings(
                            new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Mod.MOD_ID, "mod_sword"))),
                            3.0F,
                            -2.4F
                    )
            )
    );
    public static final Item MOD_AXE = registerItem("mod_axe",
            new Item(
                    ModToolMaterial.MANNIAC.applyToolSettings(
                            new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Mod.MOD_ID, "mod_axe"))),
                            BlockTags.AXE_MINEABLE, // 1. Hangi blokları hızlı kazar? (Odunlar vb.)
                            5.0F,                   // 2. Ekstra Hasar (Kılıçtan 2 birim fazla yaptık, çok güçlü!)
                            -3.0F,                  // 3. Saldırı Hızı (Balta ağırdır, -3.0 yavaştır)
                            1.5F                    // 4. Kalkan Kırma (Saniye cinsinden kalkanı ne kadar devre dışı bırakır)
                    )
            )
    );
    public static final Item MOD_PICKAXE = registerItem("mod_pickaxe",
            new Item(
                    ModToolMaterial.MANNIAC.applyToolSettings(
                            new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Mod.MOD_ID, "mod_pickaxe"))),
                            BlockTags.PICKAXE_MINEABLE, // 1. ÖNEMLİ: Taşları ve madenleri kazar
                            -68.0F,                       // 2. Hasar
                            -2.8F,                      // 3. Hız (Kılıçtan yavaş, Baltadan hızlı)
                            0.0F                        // 4. Kalkan Kırma (Kazmalar genellikle kalkan kırmaz)
                    )
            )
    );

    public static void registerModItem() {
        Mod.LOGGER.info("Eşyalar kaydediliyor: " + Mod.MOD_ID);

        // Creative Tab'e ekleme olayları ilerde buraya gelecek
    }

    // Yardımcı metot
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Mod.MOD_ID, name), item);
    }
}