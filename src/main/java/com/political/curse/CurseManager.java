package com.political.curse;

import com.political.combat.StatManager;
import com.political.curse.spirits.CursedSpiritEntity;
import com.political.curse.spirits.ModSpirits;
import com.political.curse.spirits.SpiritSpecies;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Random;

/**
 * The Curses faction (Jujutsu Kaisen). Curses are <b>custom {@link CurseEntity} mobs</b> — graded
 * cursed spirits with their own model/texture, scaled stats and a dark name — not re-skinned
 * vanilla monsters. They manifest naturally in the dark, are lured by cursed objects players
 * carry, and grant Cursed Energy + coins + grade progression when exorcised. Built on Fabric
 * server events (no mixins).
 */
public final class CurseManager {

    private static final Random RNG = new Random();

    private static int attractCounter = 0;
    private static int ambientCounter = 0;

    private CurseManager() {}

    /** Periodic curse behaviour: cursed-object lures plus ambient night manifestations. */
    public static void tick(net.minecraft.server.MinecraftServer server) {
        if (!DataManager.data().curseSpawningEnabled) return;

        // Cursed objects carried by players draw curses (checked ~every 5 seconds).
        if (++attractCounter % 100 == 0) {
            double chance = DataManager.data().cursedObjectAttractChance;
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (CursedObjects.carriesCursedObject(p) && RNG.nextDouble() < chance) {
                    spawn(p, rollNaturalGrade());
                    p.sendSystemMessage(Component.literal("The cursed object you carry stirs something nearby...")
                            .withStyle(ChatFormatting.DARK_PURPLE));
                }
            }
        }

        // Ambient manifestation: rare, in darkness, near a player (custom mob, no reskin).
        if (++ambientCounter % 200 == 0) {
            double chance = DataManager.data().curseNaturalSpawnChance;
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (!(p.level() instanceof ServerLevel level)) continue;
                if (RNG.nextDouble() >= chance) continue;
                BlockPos at = darkSpotNear(level, p);
                if (at != null) spawnAt(level, at, rollNaturalGrade());
            }
        }
    }

    public static void register() {
        // Curses are now first-class custom entities; nothing to hook on vanilla spawns.
    }

    /** Curse difficulty 1..5 -> JJK-style label (inverted: 5 is the strongest). */
    public static String gradeLabel(int grade) {
        return switch (grade) {
            case 1 -> "Grade 4 Curse";
            case 2 -> "Grade 3 Curse";
            case 3 -> "Grade 2 Curse";
            case 4 -> "Grade 1 Curse";
            case 5 -> "Special Grade Curse";
            default -> "Cursed Spirit";
        };
    }

    private static int rollNaturalGrade() {
        float r = RNG.nextFloat();
        if (r < 0.55f) return 1;
        if (r < 0.82f) return 2;
        if (r < 0.95f) return 3;
        if (r < 0.99f) return 4;
        return 5;
    }

    /** Configures a freshly created Curse to the given grade (1..5): stats, size, name, aura. */
    public static void manifest(CurseEntity curse, int grade) {
        grade = Math.max(1, Math.min(5, grade));
        curse.setGrade(grade);

        SpiritSpecies species = curse instanceof CursedSpiritEntity c ? c.species() : null;

        // Named species already carry tuned base stats, so they take a gentler grade ramp; the plain
        // CURSE_SPIRIT uses the original steeper scaling. Species size comes from its base SCALE.
        double hpMult = species != null ? 1.0 + grade * 0.35 : 1.0 + grade * 0.8;
        double dmgMult = species != null ? 1.0 + grade * 0.12 : 1.0 + grade * 0.45;

        AttributeInstance health = curse.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(health.getBaseValue() * hpMult);
            curse.setHealth(curse.getMaxHealth());
        }
        AttributeInstance dmg = curse.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(dmg.getBaseValue() * dmgMult);
        AttributeInstance sc = curse.getAttribute(Attributes.SCALE);
        if (sc != null && species == null) sc.setBaseValue(0.85f + grade * 0.18f);
        AttributeInstance kb = curse.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null) kb.setBaseValue(Math.min(1.0, kb.getBaseValue() + grade * 0.08));

        ChatFormatting color = species != null ? species.nameColor()
                : grade >= 5 ? ChatFormatting.DARK_RED
                : grade >= 4 ? ChatFormatting.RED
                : grade >= 3 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
        String label = species != null ? species.displayName() + " \u00b7 " + gradeLabel(grade) : gradeLabel(grade);
        curse.setCustomName(Component.literal("\u2620 " + label).withStyle(color));
        curse.setCustomNameVisible(species != null && species.boss());
        if (species != null && species.boss()) curse.setPersistenceRequired();

        curse.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        if (grade >= 4) curse.addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 0, false, false));
        if (grade >= 5) curse.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        if (species != null && species.has(com.political.curse.spirits.Behavior.FIRE_IMMUNE)) {
            curse.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        }
        curse.addTag("rpg_curse_" + grade);
        if (species != null) curse.addTag("rpg_curse_" + species.id());
    }

    /** Spawns a fresh Curse of the given grade near the player and returns it (or null). */
    public static CurseEntity spawn(ServerPlayer player, int grade) {
        ServerLevel level = player.level();
        BlockPos at = new BlockPos(
                (int) (player.getX() + (RNG.nextDouble() - 0.5) * 6),
                player.getBlockY(),
                (int) (player.getZ() + (RNG.nextDouble() - 0.5) * 6));
        return spawnAt(level, at, grade);
    }

    /** Spawns a fresh Curse of the given grade at a world position (random species suited to grade). */
    public static CurseEntity spawnAt(ServerLevel level, BlockPos pos, int grade) {
        if (grade >= 3 && RNG.nextBoolean()) {
            CurseEntity spirit2 = com.political.expansion2.curses.CurseSpirits2.spawnAt(level, pos, grade);
            if (spirit2 != null) return spirit2;
        }
        SpiritSpecies species = ModSpirits.isReady() ? ModSpirits.randomForGrade(grade) : null;
        return spawnSpeciesAt(level, pos, grade, species);
    }

    /** Spawns a specific cursed-spirit species (or the plain CURSE_SPIRIT when {@code species} null). */
    public static CurseEntity spawnSpeciesAt(ServerLevel level, BlockPos pos, int grade, SpiritSpecies species) {
        net.minecraft.world.entity.EntityType<? extends CurseEntity> type =
                species != null ? ModSpirits.typeFor(species) : ModEntities.CURSE_SPIRIT;
        if (type == null) type = ModEntities.CURSE_SPIRIT;
        CurseEntity curse = type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (curse == null) return null;
        curse.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        manifest(curse, grade);
        level.addFreshEntity(curse);
        return curse;
    }

    /** Finds a dark, open block near the player suitable for a curse to manifest, or null. */
    private static BlockPos darkSpotNear(ServerLevel level, ServerPlayer p) {
        for (int attempt = 0; attempt < 8; attempt++) {
            int dx = RNG.nextInt(25) - 12;
            int dz = RNG.nextInt(25) - 12;
            if (Math.abs(dx) < 6 && Math.abs(dz) < 6) continue; // not on top of the player
            int x = p.getBlockX() + dx;
            int z = p.getBlockZ() + dz;
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.isLoaded(pos)) continue;
            // Manifest only in the dark (accounts for both block light and time-of-day skylight).
            if (level.getMaxLocalRawBrightness(pos) > 7) continue;
            return pos;
        }
        return null;
    }

    public static boolean isCurse(Entity entity) {
        return entity instanceof CurseEntity
                && !com.political.expansion2.curses.CurseSpirits2.isSpirit2(entity);
    }

    private static int gradeOf(Entity entity) {
        return entity instanceof CurseEntity c ? c.getGrade() : 1;
    }

    /** Reward a player for exorcising a Curse. */
    public static void onEntityDeath(LivingEntity dead, ServerPlayer killer) {
        if (!isCurse(dead)) return;
        int grade = gradeOf(dead);
        SpiritSpecies species = dead instanceof CursedSpiritEntity c ? c.species() : null;
        int tier = species != null ? species.lootTier() : grade;
        boolean boss = species != null && species.boss();

        double energy = 8 + grade * 6 + tier * 4;
        int coins = 12 + grade * 18 + tier * 10;
        if (boss) { energy *= 2.0; coins *= 2; }

        int gained = (int) StatManager.addCursedEnergy(killer, energy);
        DataManager.addCoins(killer.getStringUUID(), coins);
        int total = DataManager.addExorcism(killer.getStringUUID());
        String label = species != null ? species.displayName() : gradeLabel(grade);
        killer.sendSystemMessage(Component.literal("Exorcised a " + label
                + "! +" + gained + " Cursed Energy, +" + coins + " coins. (" + total + " exorcised)")
                .withStyle(boss ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE));

        if (dead.level() instanceof ServerLevel level) {
            // Higher-grade / higher-tier curses leave more (and more potent) cursed remnants behind.
            int drops = boss ? 2 + RNG.nextInt(3) : (RNG.nextFloat() < 0.10f + tier * 0.08f ? 1 : 0);
            for (int i = 0; i < drops; i++) {
                net.minecraft.world.item.Item base = boss
                        ? net.minecraft.world.item.Items.WITHER_SKELETON_SKULL
                        : net.minecraft.world.item.Items.BONE;
                net.minecraft.world.item.ItemStack remnant = new net.minecraft.world.item.ItemStack(base);
                CursedObjects.makeCursed(remnant, 15 + grade * 10 + tier * 8);
                level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                        level, dead.getX(), dead.getY() + 0.5, dead.getZ(), remnant));
            }
            if (boss) {
                net.minecraft.world.item.ItemStack star =
                        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHER_STAR);
                CursedObjects.makeCursed(star, 80 + grade * 20);
                level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                        level, dead.getX(), dead.getY() + 0.7, dead.getZ(), star));
            }
        }
    }
}
