package com.mot.item.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EquipmentSlot; // EKLENDİ
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.text.Text;

public class KaosAsasi extends Item {

    public KaosAsasi(Settings settings) {
        super(settings);
    }

    // --- 1. AYARLAR ---
    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000; // Yaklaşık 1 saat
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    // --- 2. KULLANIM (UÇUŞ ve ŞARJ BAŞLANGICI) ---
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        // A) SHIFT YOKSA (UÇUŞ)
        if (!user.isSneaking()) {
            user.setVelocity(user.getRotationVector().multiply(1)); // Hız çarpanı
            user.velocityModified = true;

            user.getItemCooldownManager().set(itemStack, 50);

            // --- 1 CAN GİTME İŞLEMİ ---
            // ServerWorld kontrolü damage metodu içinde zaten yapılır, ancak
            // yine de !world.isClient bloğu içinde tutmak güvenlidir.
            if (!world.isClient()) {
                // Hangi elde tutulduğunu belirle
                EquipmentSlot slot = (hand == Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;

                // YENİ METOT: (Hasar Miktarı, Varlık, Slot)
                itemStack.damage(1, user, slot);
            }

            return ActionResult.SUCCESS;
        }

        // B) SHIFT VARSA (ŞARJ BAŞLAT)
        else {
            user.setCurrentHand(hand);
            user.playSound(SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, 3.0f, 1f);
            return ActionResult.CONSUME;
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient()) {
            int maxSure = this.getMaxUseTime(stack, user);
            int kullanilanSure = maxSure - remainingUseTicks;

            if (kullanilanSure % 10 == 0) {
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                            user.getX(), user.getY(), user.getZ(),
                            3,
                            0.5, 0.1, 0.5,
                            0.05);
                }
            }
        }
    }

    // --- 3. TUŞU BIRAKINCA (SALDIRI) ---
    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return false;

        int maxSure = this.getMaxUseTime(stack, user);
        int kullanilanSure = maxSure - remainingUseTicks;

        // Yeterince şarj edildiyse
        if (kullanilanSure >= 30) {

            if (!world.isClient()) {
                // Raycast ve Saldırı İşlemleri
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

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
                        SoundCategory.PLAYERS,
                        6.0f, 0.5f);

                // --- 5 CAN GİTME İŞLEMİ ---
                // onStoppedUsing metodunda "hand" parametresi gelmez, aktif eli bulmalıyız.
                EquipmentSlot slot = (player.getActiveHand() == Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;

                // YENİ METOT
                stack.damage(5, player, slot);
            }

            // Geri Tepme
            Vec3d lookDir = player.getRotationVector();
            player.setVelocity(player.getVelocity().add(lookDir.multiply(-0.2)));
            player.velocityModified = true;

            // Cooldown
            player.getItemCooldownManager().set(stack, 150);

        } else {
            // Şarj yetersizse
            if (!world.isClient()) {
                player.sendMessage(Text.of("§7Yeterince şarj olmadı!"), true);
            }
        }
        return true;
    }
}