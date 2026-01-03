package com.mot.mixin;

import com.mot.item.ModItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse; // HEDEF SINIF ARTIK MOUSE
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Mouse.class) // <-- BURASI DEĞİŞTİ
public class KaosCameraMixin {

    // Hedef metodu 'updateMouse' olarak değiştirdik çünkü fare hareketi orada hesaplanıyor.
    // Target kısmını da 'Entity' olarak düzelttik.
    @ModifyArgs(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;changeLookDirection(DD)V"))
    private void restrictCameraMovement(Args args) {

        // Mouse sınıfı içindeyiz, oyuncuya ulaşmak için MinecraftClient kullanıyoruz
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && player.isUsingItem()) {

            // Eğer oyuncu shift basıyorsa ve elinde Kaos Asası varsa
            if (player.isSneaking() && player.getActiveItem().isOf(ModItem.KAOS_ASASI)) {

                double deltaX = args.get(0);
                double deltaY = args.get(1);

                double yavaslatmaMiktari = 0.15; // %85 Yavaşlatma

                args.set(0, deltaX * yavaslatmaMiktari);
                args.set(1, deltaY * yavaslatmaMiktari);
            }
        }
    }
}