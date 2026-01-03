package com.mot.mixin;

import com.mot.item.ModItem;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientPlayerEntity.class)
public class KaosCameraMixin {

    @ModifyArgs(method = "changeLookDirection", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;changeLookDirection(DD)V"))
    private void restrictCameraMovement(Args args) {
        // Mixin içinde 'this' objesini ClientPlayerEntity olarak alıyoruz
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // 1. Oyuncu şu an bir eşya kullanıyor mu (Sağ tık basılı mı)?
        if (player.isUsingItem()) {

            // 2. Kullandığı eşya bizim Kaos Asası mı?
            // DİKKAT: 'ModItems.KAOS_ASASI' kısmını kendi değişken adınla değiştir.
            if (player.getActiveItem().isOf(ModItem.KAOS_ASASI)) {

                // 3. Oyuncu Shift'e basıyor mu?
                if (player.isSneaking()) {

                    // Fare hareket değerlerini al (X ve Y ekseni)
                    double deltaX = args.get(0);
                    double deltaY = args.get(1);

                    // Hassasiyeti %5'e düşür (Çok ağır döner)
                    // Tamamen kilitlemek istersen 0.0 ile çarpabilirsin.
                    double yavaslatmaMiktari = 0.05;

                    args.set(0, deltaX * yavaslatmaMiktari);
                    args.set(1, deltaY * yavaslatmaMiktari);
                }
            }
        }
    }
}