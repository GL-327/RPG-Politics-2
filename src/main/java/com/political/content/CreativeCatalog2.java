package com.political.content;

import com.political.curse.spirits.SpiritSpecies;
import com.political.expansion.armor.ArmorItems;
import com.political.expansion.armor.ArmorSet;
import com.political.expansion.melee.MeleeWeapon;
import com.political.expansion.melee.MeleeWeapons;
import com.political.expansion.mobs.ExpansionMobs;
import com.political.expansion.mobs.MobRole;
import com.political.expansion.mobs.MobSpec;
import com.political.expansion.ranged.RangedItems;
import com.political.expansion.ranged.RangedWeapon;
import com.political.expansion2.armor.Armor2Items;
import com.political.expansion2.curses.SpiritSpecies2;
import com.political.expansion2.melee.Melee2Weapon;
import com.political.expansion2.melee.Melee2Weapons;
import com.political.expansion2.mobs.ExpansionMobs2;
import com.political.expansion2.mobs.MobSpec2;
import com.political.expansion2.quests.QuestItems;
import com.political.expansion2.ranged.RangedItems2;
import com.political.expansion2.ranged.RangedWeapon2;
import com.political.items.ItemStats;
import com.political.items.RelicItems;
import com.political.items.Variant;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Helpers for creative-tab population: mob command tokens, showcase variants, relic lore. */
public final class CreativeCatalog2 {

    private static final Map<Item, String> RELIC_LORE = Map.ofEntries(
            Map.entry(RelicItems.MANA_CRYSTAL, "Restores 50% of your max Mana."),
            Map.entry(RelicItems.CURSED_ESSENCE, "Restores 40% of your max Cursed Energy."),
            Map.entry(RelicItems.EXORCISM_TOKEN, "Double rewards on your next exorcism."),
            Map.entry(RelicItems.GRADE_SCROLL, "Raises Sorcerer Grade by 1 (max 5)."),
            Map.entry(RelicItems.AWAKENING_STONE, "Awakens dormant cursed energy."),
            Map.entry(RelicItems.BOUNTY_SEAL, "Redeem for 150 coins."),
            Map.entry(RelicItems.REFORGE_STONE, "Reforges held item to next rarity."));

    private CreativeCatalog2() {}

    public static void relics(CreativeModeTab.Output out) {
        for (Item item : RelicItems.items()) {
            String lore = RELIC_LORE.getOrDefault(item, "");
            out.accept(lore.isEmpty() ? new ItemStack(item) : RelicItems.display(item, lore));
        }
    }

    public static void quests(CreativeModeTab.Output out) {
        for (Item item : QuestItems.items()) out.accept(new ItemStack(item));
    }

    public static void mobs(CreativeModeTab.Output out) {
        out.accept(mobGuide(
                "RPG Mob Showcase",
                "Summon custom creatures with operator commands.",
                List.of(
                        "/rpgmob list — expansion creatures (phase 1)",
                        "/rpgmob summon <id> [count]",
                        "/rpgmob2 list — expansion creatures (phase 2)",
                        "/rpgmob2 summon <id> [count]",
                        "/curse list — cursed spirits (phase 1)",
                        "/curse summon <species> [grade] [count]",
                        "/curse spawn <grade> — random spirit (phase 2 at grade 3+)")));

        for (MobSpec spec : ExpansionMobs.SPECS) {
            out.accept(mobToken(spec.name, roleLabel(spec.role), "/rpgmob summon " + spec.id, mobRoleColor(spec.role)));
        }
        for (MobSpec2 spec : ExpansionMobs2.SPECS) {
            out.accept(mobToken(spec.name, roleLabel(spec.role), "/rpgmob2 summon " + spec.id, mobRoleColor(spec.role)));
        }
        for (SpiritSpecies sp : SpiritSpecies.values()) {
            String grade = "Grade " + sp.gradeBand() + (sp.boss() ? " Boss" : "");
            out.accept(mobToken(sp.displayName(), grade, "/curse summon " + sp.id(), ChatFormatting.DARK_PURPLE));
        }
        for (SpiritSpecies2 sp : SpiritSpecies2.values()) {
            String grade = "Grade " + sp.gradeBand() + (sp.boss() ? " Boss" : "");
            out.accept(mobToken(sp.displayName(), grade,
                    "/curse spawn " + sp.gradeBand() + " (random; id: " + sp.id() + ")",
                    sp.nameColor() != null ? sp.nameColor() : ChatFormatting.DARK_PURPLE));
        }
    }

    public static void meleeShowcase(CreativeModeTab.Output out) {
        addVariant(out, MeleeWeapons.create(MeleeWeapon.CELESTIAL_CLAYMORE), Variant.UNIQUE);
        addCursed(out, MeleeWeapons.create(MeleeWeapon.GODSLAYER_BLADE), 4);
    }

    public static void melee2Showcase(CreativeModeTab.Output out) {
        addVariant(out, Melee2Weapons.create(Melee2Weapon.WPN2_PROM_CLAW), Variant.UNIQUE);
        addCursed(out, Melee2Weapons.create(Melee2Weapon.WPN2_CURSED_CLAW), 5);
    }

    public static void rangedShowcase(CreativeModeTab.Output out) {
        addVariant(out, RangedItems.create(RangedWeapon.VOIDSTAFF), Variant.UNIQUE);
        addCursed(out, RangedItems.create(RangedWeapon.STORMSTAFF), 3);
    }

    public static void ranged2Showcase(CreativeModeTab.Output out) {
        addVariant(out, RangedItems2.create(RangedWeapon2.VOID_WAND), Variant.UNIQUE);
        addCursed(out, RangedItems2.create(RangedWeapon2.METEOR_GUN), 4);
    }

    public static void armorShowcase(CreativeModeTab.Output out) {
        addVariant(out, ArmorItems.create(ArmorSet.CELESTIAL, ArmorSet.Slot.CHEST), Variant.UNIQUE);
        addCursed(out, ArmorItems.create(ArmorSet.TITANFORGE, ArmorSet.Slot.HELMET), 3);
    }

    public static void armor2Showcase(CreativeModeTab.Output out) {
        addVariant(out, Armor2Items.create(com.political.expansion2.armor.ArmorSet.INFINITY,
                com.political.expansion2.armor.ArmorSet.Slot.CHEST), Variant.UNIQUE);
        addCursed(out, Armor2Items.create(com.political.expansion2.armor.ArmorSet.APOCALYPSE,
                com.political.expansion2.armor.ArmorSet.Slot.HELMET), 4);
    }

    public static void accessoryShowcase(CreativeModeTab.Output out, Item flagship) {
        ItemStack unique = com.political.expansion.accessories.Accessories.display(flagship);
        addVariant(out, unique, Variant.UNIQUE);
    }

    public static void accessory2Showcase(CreativeModeTab.Output out, Item flagship) {
        ItemStack unique = com.political.expansion2.accessories.Accessories2.display(flagship);
        addVariant(out, unique, Variant.UNIQUE);
    }

    private static ItemStack mobGuide(String title, String subtitle, List<String> lines) {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(title).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(subtitle).withStyle(ChatFormatting.GRAY));
        for (String line : lines) {
            lore.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
        }
        stack.set(DataComponents.LORE, new ItemLore(lore));
        return stack;
    }

    private static ItemStack mobToken(String name, String role, String command, ChatFormatting color) {
        ItemStack stack = new ItemStack(Items.EGG);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(color, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(role).withStyle(ChatFormatting.GRAY),
                Component.literal(command).withStyle(ChatFormatting.DARK_GRAY))));
        return stack;
    }

    private static String roleLabel(MobRole role) {
        return switch (role) {
            case HOSTILE -> "Hostile";
            case NEUTRAL -> "Neutral";
            case SKITTISH -> "Skittish";
            case MINIBOSS -> "Miniboss";
            case BOSS -> "Boss";
        };
    }

    private static String roleLabel(com.political.expansion2.mobs.MobRole2 role) {
        return switch (role) {
            case HOSTILE -> "Hostile";
            case NEUTRAL -> "Neutral";
            case SKITTISH -> "Skittish";
            case MINIBOSS -> "Miniboss";
            case BOSS -> "Boss";
        };
    }

    private static ChatFormatting mobRoleColor(MobRole role) {
        return switch (role) {
            case BOSS -> ChatFormatting.DARK_RED;
            case MINIBOSS -> ChatFormatting.RED;
            case HOSTILE -> ChatFormatting.DARK_PURPLE;
            case NEUTRAL -> ChatFormatting.GREEN;
            case SKITTISH -> ChatFormatting.YELLOW;
        };
    }

    private static ChatFormatting mobRoleColor(com.political.expansion2.mobs.MobRole2 role) {
        return switch (role) {
            case BOSS -> ChatFormatting.DARK_RED;
            case MINIBOSS -> ChatFormatting.RED;
            case HOSTILE -> ChatFormatting.DARK_PURPLE;
            case NEUTRAL -> ChatFormatting.GREEN;
            case SKITTISH -> ChatFormatting.YELLOW;
        };
    }

    private static void addVariant(CreativeModeTab.Output out, ItemStack stack, Variant variant) {
        ItemStats.setVariant(stack, variant);
        ItemStats.decorate(stack);
        out.accept(stack);
    }

    private static void addCursed(CreativeModeTab.Output out, ItemStack stack, int grade) {
        ItemStats.setCursedGrade(stack, grade);
        ItemStats.decorate(stack);
        out.accept(stack);
    }
}
