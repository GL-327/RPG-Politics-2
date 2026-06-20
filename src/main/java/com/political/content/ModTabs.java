package com.political.content;

import com.political.curse.CursedGear;
import com.political.curse.CursedObjects;
import com.political.dev.DevMenuItem;
import com.political.gov.GovItems;
import com.political.items.RpgItem;
import com.political.items.RpgItems;
import com.political.power.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Registers the mod's creative tabs \u2014 one per pillar of the mod. */
public final class ModTabs {

    public static final String MOD_ID = "politicalserver";

    private ModTabs() {}

    public static void register() {
        // Powers & Serums
        tab("powers", "RPG \u2014 Powers & Serums", () -> new ItemStack(ModItems.COMPOUND_V), (params, out) -> {
            out.accept(new ItemStack(ModItems.COMPOUND_V));
            out.accept(new ItemStack(ModItems.TEMP_V));
            out.accept(new ItemStack(ModItems.V1));
            out.accept(new ItemStack(ModItems.ANTI_V));
        });

        // Cursed Arsenal
        tab("cursed", "RPG \u2014 Cursed Arsenal", () -> CursedGear.display("cursed_blade"), (params, out) -> {
            for (Item item : CursedGear.items()) {
                // build a lore'd display copy for each cursed tool
                out.accept(CursedGear.display(nameOf(item)));
            }
            out.accept(new ItemStack(Items.DRAGON_EGG)); // always a cursed object
            out.accept(cursed(Items.BONE, 18));
            out.accept(cursed(Items.WITHER_SKELETON_SKULL, 45));
            out.accept(cursed(Items.NETHER_STAR, 70));
            out.accept(cursed(Items.BELL, 40));
        });

        // RPG Gear (custom stat items)
        tab("gear", "RPG \u2014 Gear", () -> RpgItems.create(RpgItem.values()[0]), (params, out) -> {
            for (RpgItem def : RpgItem.values()) out.accept(RpgItems.create(def));
        });

        // Governance & Dev
        tab("governance", "RPG \u2014 Governance", () -> new ItemStack(GovItems.CROWN), (params, out) -> {
            for (Item item : GovItems.items()) out.accept(new ItemStack(item));
            out.accept(DevMenuItem.stack());
        });

        // Settlements & Build (the neo-medieval modern palette)
        tab("build", "RPG \u2014 Settlements & Build", () -> new ItemStack(ModBlocks.CASTLE_BRICKS), (params, out) -> {
            for (var block : ModBlocks.list()) out.accept(new ItemStack(block));
        });
    }

    private static ItemStack cursed(Item item, int amount) {
        ItemStack stack = new ItemStack(item);
        CursedObjects.makeCursed(stack, amount);
        return stack;
    }

    private static String nameOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private static void tab(String id, String title, java.util.function.Supplier<ItemStack> icon,
                            CreativeModeTab.DisplayItemsGenerator gen) {
        CreativeModeTab tab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.literal(title))
                .icon(icon)
                .displayItems(gen)
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, id), tab);
    }
}
