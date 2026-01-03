package com.mot.item.custom;

import com.mot.item.ModItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction; // 1.21.2+ için doğru yer
import net.minecraft.util.ActionResult; // Sadece bu yeterli
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
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    // --- 2. KULLANIM (SAĞ TIK) ---
    // YENİ SİSTEM: Dönüş tipi artık sadece 'ActionResult'
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        // A) SHIFT YOKSA (UÇUŞ)
        if (!user.isSneaking()) {
            user.setVelocity(user.getRotationVector().multiply(3));
            user.velocityModified = true;

            user.getItemCooldownManager().set(user.getStackInHand(user.getActiveHand()), 100);

            // Eşya değişmediği için direkt SUCCESS dönüyoruz
            return ActionResult.SUCCESS;
        }

        // B) SHIFT VARSA (ŞARJ)
        else {
            user.setCurrentHand(hand);

            // Yeni sistemde CONSUME kullanımı
            return ActionResult.CONSUME;
        }
    }

    // --- 3. TUŞU BIRAKINCA (SALDIRI) ---
    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) { // Dönüş tipi 'void' yerine 'boolean' olabilir mi kontrol et, genelde void'dir.
        if (!(user instanceof PlayerEntity player)) return false;

        int maxSure = this.getMaxUseTime(stack, user);
        int kullanilanSure = maxSure - remainingUseTicks;

        if (kullanilanSure >= 30) {

            if (!world.isClient()) {
                HitResult hit = player.raycast(50.0D, 0.0f, false);

                if (hit.getType() == HitResult.Type.BLOCK) {
                    if (world instanceof ServerWorld serverWorld) {
                        LightningEntity simsek = EntityType.LIGHTNING_BOLT.create(serverWorld, SpawnReason.TRIGGERED);
                        if (simsek != null) {
                            simsek.refreshPositionAfterTeleport(hit.getPos());
                            serverWorld.spawnEntity(simsek);
                        }
                    }
                    world.createExplosion(null, hit.getPos().x, hit.getPos().y, hit.getPos().z, 4.0f, true, World.ExplosionSourceType.TNT);
                }
            }

            player.getItemCooldownManager().set(stack, 300);

        } else {
            if (!world.isClient()) {
                player.sendMessage(Text.of("§7Yeterince şarj olmadı!"), true);
            }
        }
    }
}