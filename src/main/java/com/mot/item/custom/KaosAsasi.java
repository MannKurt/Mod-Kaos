package com.mot.item.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class KaosAsasi extends Item {

    public KaosAsasi(Settings settings) {
        super(settings);
    }

    // --- 1. AYARLAR ---
    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000; // Yaklaşık 1 saat (Şarj süresi limiti)
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW; // Yay gibi gerilme animasyonu
    }

    // --- 2. KULLANIM (SAĞ TIK BAŞLANGICI) ---
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // Elimizdeki eşyayı değişkene atıyoruz
        ItemStack itemStack = user.getStackInHand(hand);

        // A) SHIFT YOKSA (UÇUŞ)
        if (!user.isSneaking()) {
            user.setVelocity(user.getRotationVector().multiply(3));
            user.velocityModified = true;

            // DÜZELTME: user.getActiveHand() yerine yukarıda tanımladığımız 'itemStack'i veriyoruz.
            // Çünkü eşya kullanımı anlık olduğu için 'ActiveHand' bazen boş dönebilir.
            user.getItemCooldownManager().set(itemStack, 100);

            return ActionResult.SUCCESS;
        }

        // B) SHIFT VARSA (ŞARJ BAŞLAT)
        else {
            user.setCurrentHand(hand);
            return ActionResult.CONSUME;
        }
    }

    // --- 3. TUŞU BIRAKINCA (SALDIRI GERÇEKLEŞME ANI) ---
    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        // Eğer kullanan oyuncu değilse işlemi iptal et
        if (!(user instanceof PlayerEntity player)) return false;

        int maxSure = this.getMaxUseTime(stack, user);
        int kullanilanSure = maxSure - remainingUseTicks;

        // En az 30 tick (1.5 saniye) şarj edildiyse
        if (kullanilanSure >= 30) {

            if (!world.isClient()) {
                // 50 blok ileriye bak
                HitResult hit = player.raycast(50.0D, 0.0f, false);

                if (hit.getType() == HitResult.Type.BLOCK) {
                    if (world instanceof ServerWorld serverWorld) {
                        LightningEntity simsek = EntityType.LIGHTNING_BOLT.create(serverWorld, SpawnReason.TRIGGERED);
                        if (simsek != null) {
                            simsek.refreshPositionAfterTeleport(hit.getPos());
                            serverWorld.spawnEntity(simsek);
                        }
                    }
                    // Patlama yarat
                    world.createExplosion(null, hit.getPos().x, hit.getPos().y, hit.getPos().z, 4.0f, true, World.ExplosionSourceType.TNT);
                }
            }

            // DÜZELTME: Burada zaten parametre olarak gelen 'stack'i kullanıyoruz. Doğru kullanım bu.
            player.getItemCooldownManager().set(stack, 300); // 15 Saniye bekleme süresi

        } else {
            // Şarj yetersizse
            if (!world.isClient()) {
                player.sendMessage(Text.of("§7Yeterince şarj olmadı!"), true);
            }
        }

        // DÜZELTME: Metot boolean döndürmek zorunda. İşlem bittiği için true/false dönüyoruz.
        return true;
    }
}