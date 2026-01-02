package com.mot.item.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason; // YENİ EKLENDİ!
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack; // YENİ EKLENDİ!
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class KaosAsasi extends Item {

    public KaosAsasi(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {

        // --- SHIFT BASILIYSA (SALDIRI) ---
        if (user.isSneaking()) {

            // Düzeltme 1: isClient sonuna () koyduk.
            if (!world.isClient()) {
                HitResult hit = user.raycast(50.0D, 0.0f, false);

                if (hit.getType() == HitResult.Type.BLOCK) {

                    // DÜZELTME 2: create(world) yerine create(world, SpawnReason.TRIGGERED)
                    // Oyun artık "Neden doğuyor?" diye soruyor. TRIGGERED (Tetiklendi) diyoruz.
                    LightningEntity simsek = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);

                    if (simsek != null) {
                        simsek.refreshPositionAfterTeleport(hit.getPos());
                        world.spawnEntity(simsek);
                    }

                    // Patlama
                    world.createExplosion(user, hit.getPos().x, hit.getPos().y, hit.getPos().z, 4.0f, true, World.ExplosionSourceType.TNT);
                }
            }

            // DÜZELTME 3: Cooldown için ItemStack istiyor
            // "this" (Eşya) yerine "new ItemStack(this)" (Eşya Yığını) veriyoruz.
            user.getItemCooldownManager().set(new ItemStack(this), 400);
        }

        // --- SHIFT BASILI DEĞİLSE (UÇUŞ) ---
        else {
            user.setVelocity(user.getRotationVector().multiply(3));
            user.velocityModified = true;

            // DÜZELTME 3 (Tekrar): Burada da ItemStack veriyoruz.
            user.getItemCooldownManager().set(new ItemStack(this), 100);
        }

        return ActionResult.SUCCESS;
    }
}