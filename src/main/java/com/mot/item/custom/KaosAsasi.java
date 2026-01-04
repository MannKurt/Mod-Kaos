package com.mot.item.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class KaosAsasi extends Item {

    public KaosAsasi(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        // --- LAPIS KONTROLÜ ---
        boolean hasLapis = user.getAbilities().creativeMode || user.getInventory().contains(new ItemStack(Items.LAPIS_LAZULI));
        if (!hasLapis) {
            if (!world.isClient()) {
                user.sendMessage(Text.of("§9Yetersiz Lapis Lazuli!"), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            return ActionResult.FAIL;
        }

        // A) SHIFT YOKSA (HIZLI UÇUŞ - DASH)
        // Burası anlık bir işlemdir ve hemen biter. usageTick'e girmez.
        if (!user.isSneaking()) {
            user.setVelocity(user.getRotationVector().multiply(2));
            user.velocityModified = true;
            user.getItemCooldownManager().set(itemStack, 50); // Cooldown girdiği için kullanım biter
            user.setNoGravity(false);

            if (!world.isClient()) {
                EquipmentSlot slot = (hand == Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                itemStack.damage(1, user, slot);
                if (!user.getAbilities().creativeMode) {
                    user.getInventory().remove(s -> s.isOf(Items.LAPIS_LAZULI), 1, user.getInventory());
                }
            }
            return ActionResult.SUCCESS;
        }

        // B) SHIFT VARSA (ŞARJ BAŞLANGICI)
        // Burası usageTick'i başlatır.
        else {
            user.setCurrentHand(hand);
            user.playSound(SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, 1.0f, 1f);
            return ActionResult.CONSUME;
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int maxSure = this.getMaxUseTime(stack, user);
        int kullanilanSure = maxSure - remainingUseTicks;

        // --- 1. SABİT YÜKSELME (HOVER) ---
        // ARTIK SHIFT KONTROLÜ YOK!
        // Eğer buraya girdiyse (usageTick çalışıyorsa), oyuncu sağ tıka basılı tutuyordur.
        // Dash atınca cooldown girdiği için buraya gelemez. O yüzden burası sadece Şarj modudur.

        user.setNoGravity(true); // Yerçekimini kapat
        user.fallDistance = 0;   // Düşüş hasarını engelle

        Vec3d userPos = new Vec3d(user.getX(), user.getY(), user.getZ());
        Vec3d checkDown = userPos.subtract(0, 2, 0);

        HitResult groundCheck = world.raycast(new RaycastContext(
                userPos, checkDown,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                user
        ));

        Vec3d vel = user.getVelocity();
        if (groundCheck.getType() == HitResult.Type.BLOCK) {
            double distanceToGround = user.getY() - groundCheck.getPos().y;
            if (distanceToGround < 0.5) {
                // Yerden yüksel
                user.setVelocity(vel.x * 0.9, 0.05, vel.z * 0.9);
            } else {
                // Havada asılı kal (Shift bıraksan bile düşmezsin)
                user.setVelocity(vel.x * 0.9, 0.0, vel.z * 0.9);
            }
        } else {
            // Altın boşsa da asılı kal
            user.setVelocity(vel.x * 0.9, 0.0, vel.z * 0.9);
        }
        user.velocityModified = true;


        // --- 2. GÖRSEL EFEKTLER ---
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {

            // A) PENTAGRAM
            if (kullanilanSure % 2 == 0) {
                double x = user.getX();
                double y = user.getY() + 0.1;
                double z = user.getZ();
                spawnPentagram(serverWorld, x, y, z, 2.0f);
            }

            // B) RUH AURASI
            if (kullanilanSure % 4 == 0) {
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                        user.getX(), user.getY(), user.getZ(),
                        3, 0.1, 0.0, 0.1, 0.05);
            }
        }
    }

    private void spawnPentagram(ServerWorld world, double x, double y, double z, float radius) {
        double[] px = new double[5];
        double[] pz = new double[5];

        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(72 * i + (world.getTime() * 2));
            px[i] = x + radius * Math.cos(angle);
            pz[i] = z + radius * Math.sin(angle);
        }

        int[] order = {0, 2, 4, 1, 3, 0};

        for (int k = 0; k < order.length - 1; k++) {
            int startIndex = order[k];
            int endIndex = order[k + 1];

            double startX = px[startIndex];
            double startZ = pz[startIndex];
            double endX = px[endIndex];
            double endZ = pz[endIndex];

            double distance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endZ - startZ, 2));
            double steps = distance * 1.25;

            for (int s = 0; s <= steps; s++) {
                double t = s / steps;
                double currentX = startX + (endX - startX) * t;
                double currentZ = startZ + (endZ - startZ) * t;

                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        currentX, y, currentZ,
                        1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        // İŞLEM BİTTİ, ŞİMDİ YERÇEKİMİNİ GERİ AÇ
        user.setNoGravity(false);
        user.fallDistance = 0;

        if (!(user instanceof PlayerEntity player)) return false;

        int maxSure = this.getMaxUseTime(stack, user);
        int kullanilanSure = maxSure - remainingUseTicks;

        if (kullanilanSure >= 30) {
            boolean hasLapis = player.getAbilities().creativeMode || player.getInventory().contains(new ItemStack(Items.LAPIS_LAZULI));
            if (!hasLapis) {
                if (!world.isClient()) player.sendMessage(Text.of("§9Lapisin bitti!"), true);
                return false;
            }

            if (!world.isClient()) {
                double maxDistance = 50.0D;
                Vec3d startPos = player.getEyePos();
                Vec3d rotation = player.getRotationVector();
                Vec3d endPos = startPos.add(rotation.multiply(maxDistance));

                HitResult hitResult = world.raycast(new RaycastContext(startPos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
                if (hitResult.getType() != HitResult.Type.MISS) endPos = hitResult.getPos();

                Box searchBox = player.getBoundingBox().stretch(rotation.multiply(maxDistance)).expand(1.0D);
                EntityHitResult entityHitResult = ProjectileUtil.raycast(player, startPos, endPos, searchBox, (entity) -> !entity.isSpectator() && entity.canHit(), maxDistance * maxDistance);
                if (entityHitResult != null) hitResult = entityHitResult;

                if (hitResult.getType() != HitResult.Type.MISS) {
                    Vec3d targetPos = hitResult.getPos();
                    if (world instanceof ServerWorld serverWorld) {
                        LightningEntity simsek = EntityType.LIGHTNING_BOLT.create(serverWorld, SpawnReason.TRIGGERED);
                        if (simsek != null) {
                            simsek.refreshPositionAfterTeleport(targetPos);
                            serverWorld.spawnEntity(simsek);
                        }
                        world.createExplosion(null, targetPos.x, targetPos.y, targetPos.z, 4.0f, true, World.ExplosionSourceType.TNT);
                    }
                }

                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 2.0f, 0.5f);

                EquipmentSlot slot = (player.getActiveHand() == Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.damage(5, player, slot);

                if (!player.getAbilities().creativeMode) {
                    player.getInventory().remove(s -> s.isOf(Items.LAPIS_LAZULI), 1, player.getInventory());
                }
            }

            Vec3d lookDir = player.getRotationVector();
            player.setVelocity(player.getVelocity().add(lookDir.multiply(-0.5)));
            player.velocityModified = true;
            player.getItemCooldownManager().set(stack, 150);

        } else {
            if (!world.isClient()) {
                player.sendMessage(Text.of("§7Yeterince şarj olmadı!"), true);
            }
        }
        return true;
    }
}