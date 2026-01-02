package com.mot.item.custom;


import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;

public class ModToolMaterial {

    // Artık "new ToolMaterial(...)" diyerek oluşturuyoruz.
    public static final ToolMaterial MANNIAC = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, // 1. incorrectBlocksForDrops (Hangi blokları kıramaz)
            20500,                                 // 2. durability (Dayanıklılık)
            18.0F,                                 // 3. speed (Kazma Hızı)
            69.0F,                                 // 4. attackDamageBonus (Saldırı Hasarı Bonusu)
            22,                                   // 5. enchantmentValue (Büyülenme Değeri)
            ItemTags.COPPER                      // 6. repairItems (Tamir Eşyası - Artık bir ETİKET olmalı!)
    );

    // Eğer kendi özel mod eşyanla tamir edilmesini istiyorsan,
    // önce o eşya için bir Tag (Etiket) oluşturman gerekir.
    // Şimdilik örnek olarak Zümrüt (Emeralds) etiketini kullandım.
}
