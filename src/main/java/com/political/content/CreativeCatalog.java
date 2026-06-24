package com.political.content;

import com.political.curse.CursedGear;
import com.political.curse.CursedObjects;
import com.political.dev.DevMenuItem;
import com.political.expansion.accessories.Accessories;
import com.political.expansion.armor.ArmorExpansion;
import com.political.expansion.blocks.DecoBlocks;
import com.political.expansion2.blocks.DecoBlocks2;
import com.political.expansion2.accessories.Accessories2;
import com.political.expansion2.armor.Armor2Expansion;
import com.political.expansion2.food.Food2Items;
import com.political.expansion2.melee.Melee2Weapons;
import com.political.expansion2.ranged.RangedExpansion2;
import com.political.expansion.food.FoodItems;
import com.political.expansion.melee.MeleeWeapons;
import com.political.expansion.ranged.RangedExpansion;
import com.political.gov.GovItems;
import com.political.items.ItemStats;
import com.political.items.Rarity;
import com.political.items.RpgItem;
import com.political.items.RpgItems;
import com.political.items.Variant;
import com.political.power.ModItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

/** Central catalogue for populating creative tabs with every mod item and showcase variants. */
public final class CreativeCatalog {

    private CreativeCatalog() {}

    public static void powers(CreativeModeTab.Output out) {
        out.accept(new ItemStack(ModItems.COMPOUND_V));
        out.accept(new ItemStack(ModItems.TEMP_V));
        out.accept(new ItemStack(ModItems.V1));
        out.accept(new ItemStack(ModItems.ANTI_V));
    }

    public static void cursedArsenal(CreativeModeTab.Output out) {
        for (Item item : CursedGear.items()) {
            out.accept(CursedGear.display(itemId(item)));
        }
        out.accept(new ItemStack(Items.DRAGON_EGG));
        for (Map.Entry<Item, Integer> e : CursedObjects.eligible().entrySet()) {
            out.accept(cursed(e.getKey(), e.getValue()));
        }
    }

    public static void gear(CreativeModeTab.Output out) {
        for (RpgItem def : RpgItem.values()) {
            out.accept(RpgItems.create(def));
        }
        addVariantSample(out, RpgItem.VOID_REAVER, Variant.UNIQUE);
        addVariantSample(out, RpgItem.DRAGONSLAYER, Variant.UNIQUE);
        addVariantSample(out, RpgItem.STORM_PLATE, Variant.UNIQUE);
        addCursedSample(out, RpgItem.ABYSSAL_BLADE, 3);
        addCursedSample(out, RpgItem.SKULL_MACE, 4);
        out.accept(decoratedVanilla(new ItemStack(Items.NETHERITE_SWORD), Rarity.LEGENDARY));
        out.accept(decoratedVanilla(new ItemStack(Items.DIAMOND_PICKAXE), Rarity.EPIC));
        out.accept(decoratedVanilla(new ItemStack(Items.BOW), Rarity.RARE));
    }

    public static void relics(CreativeModeTab.Output out) {
        CreativeCatalog2.relics(out);
    }

    public static void governance(CreativeModeTab.Output out) {
        for (Item item : GovItems.items()) out.accept(new ItemStack(item));
        out.accept(DevMenuItem.stack());
    }

    public static void build(CreativeModeTab.Output out) {
        for (var block : ModBlocks.list()) out.accept(new ItemStack(block));
        for (var block : com.political.world.dungeons.DungeonBlocks.list()) out.accept(new ItemStack(block));
        for (var block : com.political.world.structures.StructureBlocks.list()) out.accept(new ItemStack(block));
        for (var block : DecoBlocks.blocks()) out.accept(new ItemStack(block));
        for (var block : DecoBlocks2.blocks()) out.accept(new ItemStack(block));
        out.accept(com.political.world.dungeons.StructureCompassItem.stack());
    }

    public static void melee(CreativeModeTab.Output out) {
        for (ItemStack stack : MeleeWeapons.displays()) out.accept(stack);
        CreativeCatalog2.meleeShowcase(out);
    }

    public static void ranged(CreativeModeTab.Output out) {
        for (ItemStack stack : RangedExpansion.items()) out.accept(stack);
        CreativeCatalog2.rangedShowcase(out);
    }

    public static void armor(CreativeModeTab.Output out) {
        for (ItemStack stack : ArmorExpansion.items()) out.accept(stack);
        CreativeCatalog2.armorShowcase(out);
    }

    public static void accessories(CreativeModeTab.Output out) {
        for (Item item : Accessories.items()) out.accept(Accessories.display(item));
        CreativeCatalog2.accessoryShowcase(out, godslayerRelic());
    }

    public static void food(CreativeModeTab.Output out) {
        for (Item item : FoodItems.items()) out.accept(new ItemStack(item));
    }

    public static void melee2(CreativeModeTab.Output out) {
        for (ItemStack stack : Melee2Weapons.displays()) out.accept(stack);
        CreativeCatalog2.melee2Showcase(out);
    }

    public static void ranged2(CreativeModeTab.Output out) {
        for (ItemStack stack : RangedExpansion2.items()) out.accept(stack);
        CreativeCatalog2.ranged2Showcase(out);
    }

    public static void armor2(CreativeModeTab.Output out) {
        for (ItemStack stack : Armor2Expansion.items()) out.accept(stack);
        CreativeCatalog2.armor2Showcase(out);
    }

    public static void accessories2(CreativeModeTab.Output out) {
        for (Item item : Accessories2.items()) out.accept(Accessories2.display(item));
        for (Item item : com.political.expansion2.accessories.AbilityAccessories2.items()) {
            out.accept(com.political.expansion2.accessories.AbilityAccessories2.display(item));
        }
        CreativeCatalog2.accessory2Showcase(out, Accessories2.items().getFirst());
    }

    public static void food2(CreativeModeTab.Output out) {
        for (Item item : Food2Items.items()) out.accept(new ItemStack(item));
    }

    public static void echoArchive(CreativeModeTab.Output out) {
        out.accept(com.political.echo.EchoItems.display(com.political.echo.EchoItems.GLASSBOUND_CODEX,
                "Opens the Echo Archive — fragments of cursed history."));
        out.accept(com.political.echo.EchoItems.display(com.political.echo.EchoItems.VEILSTONE_LENS,
                "Resonates toward the nearest recorded structure or village."));
        out.accept(com.political.echo.EchoItems.display(com.political.echo.EchoItems.RESONANCE_AMPOULE,
                "Restores a surge of cursed energy."));
        out.accept(com.political.echo.EchoItems.display(com.political.echo.EchoItems.MNEMONIC_SEAL,
                "Imprints your current position into the seal's memory."));
    }

    public static void quests(CreativeModeTab.Output out) {
        CreativeCatalog2.quests(out);
    }

    public static void mobs(CreativeModeTab.Output out) {
        CreativeCatalog2.mobs(out);
    }

    private static Item godslayerRelic() {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(
                net.minecraft.resources.Identifier.fromNamespaceAndPath(ModTabs.MOD_ID, "acc_godslayer_relic"));
    }

    private static ItemStack cursed(Item item, int amount) {
        ItemStack stack = new ItemStack(item);
        CursedObjects.makeCursed(stack, amount);
        return stack;
    }

    private static String itemId(Item item) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private static void addVariantSample(CreativeModeTab.Output out, RpgItem def, Variant variant) {
        ItemStack stack = RpgItems.create(def);
        ItemStats.setVariant(stack, variant);
        ItemStats.decorate(stack);
        out.accept(stack);
    }

    private static void addCursedSample(CreativeModeTab.Output out, RpgItem def, int grade) {
        ItemStack stack = RpgItems.create(def);
        ItemStats.setCursedGrade(stack, grade);
        ItemStats.decorate(stack);
        out.accept(stack);
    }

    private static ItemStack decoratedVanilla(ItemStack stack, Rarity rarity) {
        ItemStats.setRarity(stack, rarity);
        ItemStats.decorate(stack);
        return stack;
    }
}
