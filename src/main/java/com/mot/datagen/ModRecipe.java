package com.mot.datagen;

import com.mot.item.ModItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModRecipe extends FabricRecipeProvider {
    public ModRecipe(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }


    @Override
    public String getName() {
        return "Mod Recipes";
    }

    @Override
    public RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
        return new RecipeGenerator(registries, exporter) {
            @Override
            public void generate() {
                ShapedRecipeJsonBuilder.create(registries.getOrThrow(RegistryKeys.ITEM), RecipeCategory.MISC, ModItem.LOKUM, 6)
                        .pattern(" # ")
                        .pattern("WXW")
                        .pattern("SSS")
                        .input('#', Items.POPPY)
                        .input('W', Items.WHEAT)
                        .input('S', Items.SUGAR)
                        .input('X', Items.WATER_BUCKET)

                        .criterion("has_sugar", conditionsFromItem(Items.SUGAR))
                        .criterion("has_wheat", conditionsFromItem(Items.WHEAT))
                        .criterion("has_water_bucket", conditionsFromItem(Items.WATER_BUCKET))
                        .criterion("has_poppy", conditionsFromItem(Items.POPPY))


                        .offerTo(exporter);


                // Buraya başka tarifler ekleyebilirsin
            }
        };
    }
}
