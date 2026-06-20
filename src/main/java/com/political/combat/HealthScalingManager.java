package com.political.combat;

import com.political.politics.DataManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

import java.util.Random;

/**
 * Spawns occasional "elite/champion/enchanted" variants of hostile mobs with scaled
 * health, damage, a coloured name tag, and (for enchanted) a glow. Implemented with
 * the Fabric entity-load event and a one-time tag marker (no mixins).
 */
public final class HealthScalingManager {

    private static final Random RNG = new Random();
    private static final String MARK = "rpg_scaled";

    private HealthScalingManager() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof Monster mob)) return;
            if (mob.hasCustomName()) return; // already scaled or exempted (bosses keep a name)
            if (!DataManager.data().healthScalingEnabled) return;
            if (RNG.nextFloat() >= 0.30f) return;
            applyVariant(mob);
        });
    }

    private enum Variant {
        ELITE("Elite", ChatFormatting.YELLOW, 1.5, 1.2, false),
        CHAMPION("Champion", ChatFormatting.GOLD, 2.5, 1.6, false),
        ENCHANTED("Enchanted", ChatFormatting.LIGHT_PURPLE, 4.0, 2.0, true);

        final String label;
        final ChatFormatting color;
        final double hp;
        final double dmg;
        final boolean glow;

        Variant(String label, ChatFormatting color, double hp, double dmg, boolean glow) {
            this.label = label;
            this.color = color;
            this.hp = hp;
            this.dmg = dmg;
            this.glow = glow;
        }
    }

    private static Variant roll() {
        float r = RNG.nextFloat();
        if (r < 0.60f) return Variant.ELITE;
        if (r < 0.90f) return Variant.CHAMPION;
        return Variant.ENCHANTED;
    }

    private static void applyVariant(Monster mob) {
        Variant v = roll();
        double globalMult = DataManager.data().mobHealthScalingMultiplier;

        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(health.getBaseValue() * v.hp * globalMult);
            mob.setHealth(mob.getMaxHealth());
        }
        AttributeInstance dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) {
            dmg.setBaseValue(dmg.getBaseValue() * v.dmg);
        }

        String base = mob.getType().getDescription().getString();
        mob.setCustomName(Component.literal(v.label + " " + base).withStyle(v.color));
        mob.setCustomNameVisible(true);
        if (v.glow) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        }
        mob.addTag(MARK);
    }

    /** Marks an entity so it is never auto-scaled (used by bounty bosses). */
    public static void exempt(LivingEntity entity) {
        entity.addTag(MARK);
    }
}
