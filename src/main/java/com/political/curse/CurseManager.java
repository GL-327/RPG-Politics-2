package com.political.curse;

import com.political.combat.StatManager;
import com.political.politics.DataManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

import java.util.Random;

/**
 * The Curses faction (Jujutsu Kaisen). Hostile mobs are occasionally manifested as
 * graded Curses \u2014 cursed spirits with scaled stats, a glow, and a dark name. Slaying
 * (exorcising) a Curse grants Cursed Energy and coins and advances the player's
 * sorcerer grade. Built on the Fabric entity-load + death events (no mixins).
 */
public final class CurseManager {

    private static final Random RNG = new Random();
    private static final String CURSE_WORD = "Curse";

    private static int attractCounter = 0;

    private CurseManager() {}

    /** Cursed objects carried by players draw curses; checked roughly every 5 seconds. */
    public static void tick(net.minecraft.server.MinecraftServer server) {
        if (!DataManager.data().curseSpawningEnabled) return;
        if (++attractCounter % 100 != 0) return;
        double chance = DataManager.data().cursedObjectAttractChance;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (CursedObjects.carriesCursedObject(p) && RNG.nextDouble() < chance) {
                spawn(p, rollNaturalGrade());
                p.sendSystemMessage(Component.literal("The cursed object you carry stirs something nearby...")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof Monster mob)) return;
            if (mob.hasCustomName()) return; // already special (scaled/boss/curse)
            if (!DataManager.data().curseSpawningEnabled) return;
            if (RNG.nextFloat() >= DataManager.data().curseNaturalSpawnChance) return;
            manifest(mob, rollNaturalGrade());
        });
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

    /** Turns a hostile mob into a Curse of the given grade (1..5), in place. */
    public static void manifest(Monster mob, int grade) {
        grade = Math.max(1, Math.min(5, grade));
        double hpMult = 1.5 + grade * 1.1;
        double dmgMult = 1.0 + grade * 0.5;

        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(health.getBaseValue() * hpMult);
            mob.setHealth(mob.getMaxHealth());
        }
        AttributeInstance dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(dmg.getBaseValue() * dmgMult);
        AttributeInstance kbResist = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbResist != null) kbResist.setBaseValue(Math.min(1.0, kbResist.getBaseValue() + grade * 0.15));

        ChatFormatting color = grade >= 5 ? ChatFormatting.DARK_RED
                : grade >= 4 ? ChatFormatting.RED
                : grade >= 3 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE;
        mob.setCustomName(Component.literal("\u2620 " + gradeLabel(grade)).withStyle(color));
        mob.setCustomNameVisible(true);
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        if (grade >= 4) mob.addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 1, false, false));
        if (grade >= 5) mob.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        mob.addTag("rpg_scaled"); // keep generic health-scaling away
        mob.addTag("rpg_curse_" + grade);
    }

    /** Spawns a fresh Curse of the given grade at the player and returns it (or null). */
    public static Monster spawn(ServerPlayer player, int grade) {
        ServerLevel level = player.level();
        EntityType<? extends Monster> type = baseFor(grade);
        Monster mob = type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return null;
        mob.setPos(player.getX() + (RNG.nextDouble() - 0.5) * 4, player.getY(), player.getZ() + (RNG.nextDouble() - 0.5) * 4);
        manifest(mob, grade);
        level.addFreshEntity(mob);
        return mob;
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Monster> baseFor(int grade) {
        // Higher grades manifest as true custom Curses; lower grades borrow vanilla shells.
        return (EntityType<? extends Monster>) switch (grade) {
            case 1 -> EntityTypes.ZOMBIE;
            case 2 -> EntityTypes.HUSK;
            default -> ModEntities.CURSE_SPIRIT;
        };
    }

    public static boolean isCurse(Entity entity) {
        if (!(entity instanceof LivingEntity) || !entity.hasCustomName()) return false;
        Component name = entity.getCustomName();
        return name != null && name.getString().contains(CURSE_WORD);
    }

    private static int gradeOf(Entity entity) {
        Component name = entity.getCustomName();
        String s = name == null ? "" : name.getString();
        if (s.contains("Special Grade")) return 5;
        if (s.contains("Grade 1")) return 4;
        if (s.contains("Grade 2")) return 3;
        if (s.contains("Grade 3")) return 2;
        return 1;
    }

    /** Reward a player for exorcising a Curse. */
    public static void onEntityDeath(LivingEntity dead, ServerPlayer killer) {
        if (!isCurse(dead)) return;
        int grade = gradeOf(dead);
        double energy = 8 + grade * 6;
        int coins = 12 + grade * 18;
        int gained = (int) StatManager.addCursedEnergy(killer, energy);
        DataManager.addCoins(killer.getStringUUID(), coins);
        int total = DataManager.addExorcism(killer.getStringUUID());
        killer.sendSystemMessage(Component.literal("Exorcised a " + gradeLabel(grade)
                + "! +" + gained + " Cursed Energy, +" + coins + " coins. (" + total + " exorcised)")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
