package com.mot.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items; // EKLENDİ
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.text.Text; // EKLENDİ

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

    // --- 2. KULLANIM (UÇUŞ ve ŞARJ BAŞLANGICI) ---
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        // --- LAPIS KONTROLÜ (GENEL) ---
        boolean hasLapis = user.getAbilities().creativeMode || ((PlayerEntity)user).getInventory().contains(new ItemStack(Items.LAPIS_LAZULI));
        if (!hasLapis) {
            if (!world.isClient()) {
                ((PlayerEntity)user).sendMessage(Text.of("§9Yetersiz Lapis Lazuli!"), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            return ActionResult.FAIL;
        }

        // A) SHIFT YOKSA (UÇUŞ)
        if (!user.isSneaking()) {
            user.setVelocity(user.getRotationVector().multiply(2));
            user.velocityModified = true;

            user.getItemCooldownManager().set(itemStack, 50);

            if (!world.isClient()) {
                EquipmentSlot slot = (hand == Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                itemStack.damage(1, user, slot);

                // Lapis Sil (Uçuş için)
                if (!user.getAbilities().creativeMode) {
                    ((PlayerEntity)user).getInventory().remove(s -> s.isOf(Items.LAPIS_LAZULI), 1, ((PlayerEntity)user).getInventory());
                }
            }

            return ActionResult.SUCCESS;
        }

        // B) SHIFT VARSA (ŞARJ BAŞLAT)
        else {
            user.setCurrentHand(hand);
            user.playSound(SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, 1.0f, 1f);
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

        // Yeterince şarj edildiyse (30 tick = 1.5 saniye)
        if (kullanilanSure >= 30) {

            // --- LAPIS KONTROLÜ (ATEŞLEME) ---
            boolean hasLapis = player.getAbilities().creativeMode || player.getInventory().contains(new ItemStack(Items.LAPIS_LAZULI));
            if (!hasLapis) {
                if (!world.isClient()) player.sendMessage(Text.of("§9Lapisin bitti!"), true);
                return false;
            }

            if (!world.isClient()) {

                // --- GELİŞMİŞ RAYCAST BAŞLANGICI (Entity + Blok) ---
                double maxDistance = 50.0D;
                Vec3d startPos = player.getEyePos();
                Vec3d rotation = player.getRotationVector();
                Vec3d endPos = startPos.add(rotation.multiply(maxDistance));

                // 1. Adım: Önce bloğa çarpıyor mu?
                HitResult hitResult = world.raycast(new RaycastContext(
                        startPos, endPos,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        player
                ));

                // Eğer duvara çarptıysa tarama mesafesini o duvara kadar kısaltıyoruz
                if (hitResult.getType() != HitResult.Type.MISS) {
                    endPos = hitResult.getPos();
                }

                // 2. Adım: Oyuncu ile duvar arasındaki canlıları tara
                Box searchBox = player.getBoundingBox().stretch(rotation.multiply(maxDistance)).expand(1.0D);
                EntityHitResult entityHitResult = ProjectileUtil.raycast(
                        player,
                        startPos,
                        endPos,
                        searchBox,
                        (entity) -> !entity.isSpectator() && entity.canHit(),
                        maxDistance * maxDistance
                );

                // Eğer bir canlıya çarptıysak, ana hedefimiz artık o canlıdır
                if (entityHitResult != null) {
                    hitResult = entityHitResult;
                }

                // --- HEDEF VURMA İŞLEMLERİ ---
                if (hitResult.getType() != HitResult.Type.MISS) {
                    Vec3d targetPos = hitResult.getPos();

                    if (world instanceof ServerWorld serverWorld) {
                        // A) Şimşek Çağır
                        LightningEntity simsek = EntityType.LIGHTNING_BOLT.create(serverWorld, SpawnReason.TRIGGERED);
                        if (simsek != null) {
                            simsek.refreshPositionAfterTeleport(targetPos);
                            serverWorld.spawnEntity(simsek);
                        }

                        // B) Patlama Yarat
                        world.createExplosion(null, targetPos.x, targetPos.y, targetPos.z, 4.0f, true, World.ExplosionSourceType.TNT);
                    }
                }

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
                        SoundCategory.PLAYERS,
                        2.0f, 0.5f);

                // --- EŞYA VE CEPHANE TÜKETİMİ ---
                EquipmentSlot slot = (player.getActiveHand() == Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.damage(5, player, slot);

                // Lapis Sil (Saldırı için)
                if (!player.getAbilities().creativeMode) {
                    player.getInventory().remove(s -> s.isOf(Items.LAPIS_LAZULI), 1, player.getInventory());
                }
            }

            // Geri Tepme
            Vec3d lookDir = player.getRotationVector();
            player.setVelocity(player.getVelocity().add(lookDir.multiply(-0.5)));
            player.velocityModified = true;

            // Cooldown
            player.getItemCooldownManager().set(stack, 150);

        } else {
            if (!world.isClient()) {
                player.sendMessage(Text.of("§7Yeterince şarj olmadı!"), true);
            }
        }
        return true;
    }
}