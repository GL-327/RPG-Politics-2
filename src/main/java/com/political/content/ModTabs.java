package com.political.content;

import com.political.curse.CursedGear;
import com.political.expansion.accessories.Accessories;
import com.political.expansion.armor.ArmorExpansion;
import com.political.expansion.food.FoodItems;
import com.political.expansion.melee.MeleeWeapons;
import com.political.expansion.ranged.RangedExpansion;
import com.political.expansion2.accessories.Accessories2;
import com.political.expansion2.armor.Armor2Expansion;
import com.political.expansion2.food.Food2Items;
import com.political.expansion2.melee.Melee2Weapons;
import com.political.expansion2.ranged.RangedExpansion2;
import com.political.gov.GovItems;
import com.political.items.RelicItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Registers the mod's creative tabs — complete catalogue of all content. */
public final class ModTabs {

    public static final String MOD_ID = "politicalserver";

    private ModTabs() {}

    public static void register() {
        tab("powers", CreativeModeTab.Row.TOP, 0,
                () -> new ItemStack(com.political.power.ModItems.COMPOUND_V),
                (params, out) -> CreativeCatalog.powers(out));

        tab("cursed", CreativeModeTab.Row.TOP, 1,
                () -> CursedGear.display("cursed_blade"),
                (params, out) -> CreativeCatalog.cursedArsenal(out));

        tab("gear", CreativeModeTab.Row.TOP, 2,
                () -> com.political.items.RpgItems.create(com.political.items.RpgItem.AEGIS_BLADE),
                (params, out) -> CreativeCatalog.gear(out));

        tab("relics", CreativeModeTab.Row.TOP, 3,
                () -> RelicItems.display(RelicItems.MANA_CRYSTAL, "Restores 50% of your max Mana."),
                (params, out) -> CreativeCatalog.relics(out));

        tab("governance", CreativeModeTab.Row.TOP, 4,
                () -> new ItemStack(GovItems.CROWN),
                (params, out) -> CreativeCatalog.governance(out));

        tab("build", CreativeModeTab.Row.TOP, 5,
                () -> new ItemStack(ModBlocks.CASTLE_BRICKS),
                (params, out) -> CreativeCatalog.build(out));

        tab("mobs", CreativeModeTab.Row.TOP, 6,
                () -> new ItemStack(net.minecraft.world.item.Items.EGG),
                (params, out) -> CreativeCatalog.mobs(out));

        tab("quests", CreativeModeTab.Row.TOP, 7,
                () -> new ItemStack(com.political.expansion2.quests.QuestItems.get("quest2_bounty_seal")),
                (params, out) -> CreativeCatalog.quests(out));

        tab("melee", CreativeModeTab.Row.BOTTOM, 0,
                () -> MeleeWeapons.displays().getFirst(),
                (params, out) -> CreativeCatalog.melee(out));

        tab("ranged", CreativeModeTab.Row.BOTTOM, 1,
                () -> RangedExpansion.items().getFirst(),
                (params, out) -> CreativeCatalog.ranged(out));

        tab("armor", CreativeModeTab.Row.BOTTOM, 2,
                () -> ArmorExpansion.items().getFirst(),
                (params, out) -> CreativeCatalog.armor(out));

        tab("accessories", CreativeModeTab.Row.BOTTOM, 3,
                () -> Accessories.display(godslayerRelic()),
                (params, out) -> CreativeCatalog.accessories(out));

        tab("food", CreativeModeTab.Row.BOTTOM, 4,
                () -> new ItemStack(FoodItems.items().getFirst()),
                (params, out) -> CreativeCatalog.food(out));

        tab("melee2", CreativeModeTab.Row.BOTTOM, 5,
                () -> Melee2Weapons.displays().getFirst(),
                (params, out) -> CreativeCatalog.melee2(out));

        tab("ranged2", CreativeModeTab.Row.BOTTOM, 6,
                () -> RangedExpansion2.items().getFirst(),
                (params, out) -> CreativeCatalog.ranged2(out));

        tab("armor2", CreativeModeTab.Row.BOTTOM, 7,
                () -> Armor2Expansion.items().getFirst(),
                (params, out) -> CreativeCatalog.armor2(out));

        tab("accessories2", CreativeModeTab.Row.BOTTOM, 8,
                () -> Accessories2.display(Accessories2.items().getFirst()),
                (params, out) -> CreativeCatalog.accessories2(out));

        tab("food2", CreativeModeTab.Row.BOTTOM, 9,
                () -> new ItemStack(Food2Items.items().getFirst()),
                (params, out) -> CreativeCatalog.food2(out));

        tab("echo_archive", CreativeModeTab.Row.BOTTOM, 10,
                () -> com.political.echo.EchoItems.display(com.political.echo.EchoItems.GLASSBOUND_CODEX,
                        "Opens the Echo Archive — fragments of cursed history."),
                (params, out) -> CreativeCatalog.echoArchive(out));
    }

    private static Item godslayerRelic() {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(MOD_ID, "acc_godslayer_relic"));
    }

    private static void tab(String id, CreativeModeTab.Row row, int column,
                            java.util.function.Supplier<ItemStack> icon,
                            CreativeModeTab.DisplayItemsGenerator gen) {
        CreativeModeTab tab = CreativeModeTab.builder(row, column)
                .title(Component.translatable("itemGroup." + MOD_ID + "." + id))
                .icon(icon)
                .displayItems(gen)
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, id), tab);
    }
}
