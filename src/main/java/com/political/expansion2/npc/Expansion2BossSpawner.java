package com.political.expansion2.npc;

import com.political.expansion2.quests.Expansion2QuestManager;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Expansion2BossSpawner {

    private static final Map<UUID, NamedNpcBoss> ACTIVE = new HashMap<>();
    private static final Map<NamedNpcBoss, NpcArchetype> HINT_MAP = new EnumMap<>(NamedNpcBoss.class);

    static {
        HINT_MAP.put(NamedNpcBoss.VALDRIS, NpcArchetype.CURSED_MERCHANT);
        HINT_MAP.put(NamedNpcBoss.MORGRIM, NpcArchetype.ARMOR_SMITH);
        HINT_MAP.put(NamedNpcBoss.SYLVA, NpcArchetype.ENCHANTER);
        HINT_MAP.put(NamedNpcBoss.RAZIEL, NpcArchetype.BOUNTY_BROKER);
        HINT_MAP.put(NamedNpcBoss.CROFT, NpcArchetype.BANKER);
        HINT_MAP.put(NamedNpcBoss.BLACKWOOD, NpcArchetype.POLITICIAN);
        HINT_MAP.put(NamedNpcBoss.MORBIDIUS, NpcArchetype.HEALER);
        HINT_MAP.put(NamedNpcBoss.ASHARA, NpcArchetype.SORCERER);
        HINT_MAP.put(NamedNpcBoss.GARRICK, NpcArchetype.MERCENARY);
        HINT_MAP.put(NamedNpcBoss.KAGURO, NpcArchetype.SPIRIT_HUNTER);
        HINT_MAP.put(NamedNpcBoss.THORN, NpcArchetype.ELECTION_CLERK);
        HINT_MAP.put(NamedNpcBoss.PYRION, NpcArchetype.DOOMSDAY_PROPHET);
    }

    private Expansion2BossSpawner() {}

    public static NamedNpcBoss hintForArchetype(NpcArchetype arch) {
        for (var e : HINT_MAP.entrySet()) if (e.getValue() == arch) return e.getKey();
        return null;
    }

    public static boolean spawn(ServerPlayer player, NamedNpcBoss boss) {
        ServerLevel level = player.level();
        Vindicator mob = EntityTypes.VINDICATOR.create(level, EntitySpawnReason.TRIGGERED);
        if (mob == null) return false;

        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) hp.setBaseValue(boss.baseHealth);
        var dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(boss.baseDamage);
        var spd = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) spd.setBaseValue(boss.moveSpeed);
        mob.setHealth((float) boss.baseHealth);

        mob.setCustomName(Component.literal(boss.displayName + " [BOSS]").withStyle(boss.color, ChatFormatting.BOLD));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        mob.setDropChance(EquipmentSlot.MAINHAND, 0f);
        mob.setPos(player.getX() + 4, player.getY(), player.getZ() + 4);
        level.addFreshEntity(mob);
        ACTIVE.put(mob.getUUID(), boss);
        player.sendSystemMessage(Component.literal(boss.displayName + " has appeared!").withStyle(ChatFormatting.DARK_RED));
        return true;
    }

    public static void onEntityDeath(LivingEntity victim, ServerPlayer killer) {
        NamedNpcBoss boss = ACTIVE.remove(victim.getUUID());
        if (boss == null) return;
        String uuid = killer.getStringUUID();
        int coins = 200 + (int) (boss.baseHealth / 2);
        DataManager.addCoins(uuid, coins);
        Expansion2QuestManager.onBossDefeated(killer, boss);
        killer.sendSystemMessage(Component.literal("Boss defeated: " + boss.displayName + "! +" + coins + " coin.")
                .withStyle(ChatFormatting.GOLD));
    }

    public static void tickEnrage(net.minecraft.server.MinecraftServer server) {
        for (UUID id : new HashMap<>(ACTIVE).keySet()) {
            for (ServerLevel level : server.getAllLevels()) {
                var ent = level.getEntity(id);
                if (ent instanceof LivingEntity le && le.isAlive()) {
                    if (le.getHealth() < le.getMaxHealth() * 0.35f && !le.hasEffect(MobEffects.STRENGTH)) {
                        le.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 99999, 1, false, true));
                        le.addEffect(new MobEffectInstance(MobEffects.SPEED, 99999, 0, false, true));
                    }
                    break;
                }
            }
        }
    }
}
