package com.mot.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items; // EKLENDİ
import net.minecraft.item.consume.UseAction;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text; // EKLENDİ
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class LazerAsasi extends Item {

    public LazerAsasi(Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // --- KIZILTAŞ KONTROLÜ (BAŞLANGIÇ) ---
        if (!user.getAbilities().creativeMode && !user.getInventory().contains(new ItemStack(Items.REDSTONE))) {
            if (!world.isClient()) {
                user.sendMessage(Text.of("§cYetersiz Kızıltaş!"), true);
            }
            return ActionResult.FAIL;
        }

        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.PLAYERS, 1.0f, 2.0f);
        return ActionResult.CONSUME;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return false;

        int maxSure = this.getMaxUseTime(stack, user);
        int kullanilanSure = maxSure - remainingUseTicks;

        if (kullanilanSure >= 20) {

            // --- KIZILTAŞ KONTROLÜ (ATEŞLEME) ---
            boolean hasAmmo = player.getAbilities().creativeMode || player.getInventory().contains(new ItemStack(Items.REDSTONE));
            if (!hasAmmo) {
                if (!world.isClient()) {
                    player.sendMessage(Text.of("§cKızıltaş Eksik"), true);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
                return false;
            }

            if (!world.isClient()) {

                // 1. IŞIN TARAMASI (Raycast)
                double maxDistance = 64.0D;
                Vec3d startPos = player.getEyePos();
                Vec3d rotation = player.getRotationVector();
                Vec3d endPos = startPos.add(rotation.multiply(maxDistance));

                // A) Blok Kontrolü
                HitResult hitResult = world.raycast(new RaycastContext(
                        startPos, endPos,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        player
                ));

                if (hitResult.getType() != HitResult.Type.MISS) {
                    endPos = hitResult.getPos();
                }

                // B) Entity Kontrolü
                Box box = player.getBoundingBox().stretch(rotation.multiply(maxDistance)).expand(1.0D, 1.0D, 1.0D);
                EntityHitResult entityHitResult = ProjectileUtil.raycast(
                        player,
                        startPos,
                        endPos,
                        box,
                        (entity) -> !entity.isSpectator() && entity.canHit(),
                        maxDistance * maxDistance
                );

                if (entityHitResult != null) {
                    hitResult = entityHitResult;
                }

                // 2. HASAR VE EFEKTLER
                Vec3d targetPos = hitResult.getPos();
                Vec3d laserStart = player.getEyePos().subtract(0, 0.3, 0);

                if (world instanceof ServerWorld serverWorld) {
                    spawnLaserParticles(serverWorld, laserStart, targetPos);

                    world.createExplosion(null, targetPos.x, targetPos.y, targetPos.z,
                            1.0f, World.ExplosionSourceType.NONE);

                    Box killBox = new Box(
                            targetPos.x - 3, targetPos.y - 3, targetPos.z - 3,
                            targetPos.x + 3, targetPos.y + 3, targetPos.z + 3
                    );

                    // Alan Hasarı
                    List<LivingEntity> victims = world.getEntitiesByClass(LivingEntity.class, killBox, entity -> entity != player);
                    for (LivingEntity victim : victims) {
                        victim.damage(serverWorld, world.getDamageSources().playerAttack(player), 5.0f);
                    }

                    // Direkt Vurulan Kişiye Ekstra Hasar
                    if (hitResult.getType() == HitResult.Type.ENTITY && hitResult instanceof EntityHitResult entityHit) {
                        Entity hitEntity = entityHit.getEntity();
                        hitEntity.damage(serverWorld, world.getDamageSources().playerAttack(player), 10.0f);
                    }
                }

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 4.0f, 2.0f);

                // Geri Tepme
                Vec3d lookDir = player.getRotationVector();
                player.setVelocity(player.getVelocity().add(lookDir.multiply(-0.5)));
                player.velocityModified = true;

                // --- EŞYA VE CEPHANE TÜKETİMİ ---
                if (user instanceof ServerPlayerEntity serverPlayer) {
                    EquipmentSlot slot = (player.getActiveHand() == Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    stack.damage(2, serverPlayer, slot);

                    // Kızıltaş Sil
                    if (!player.getAbilities().creativeMode) {
                        player.getInventory().remove(item -> item.isOf(Items.REDSTONE), 1, player.getInventory());
                    }
                }

                player.getItemCooldownManager().set(stack, 0);
            }
        }
        return true;
    }

    private void spawnLaserParticles(ServerWorld world, Vec3d start, Vec3d end) {
        double distance = start.distanceTo(end);
        Vec3d direction = end.subtract(start).normalize();
        int redColor = 0xFF0000;

        for (double i = 0; i < distance; i += 0.1) {
            Vec3d currentPos = start.add(direction.multiply(i));
            world.spawnParticles(
                    new DustParticleEffect(redColor, 2.0f),
                    currentPos.x, currentPos.y, currentPos.z,
                    1, 0, 0, 0, 0
            );
        }
    }
}