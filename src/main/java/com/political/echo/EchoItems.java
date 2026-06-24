package com.political.echo;

import com.political.combat.StatManager;
import com.political.net.EchoCodexOpenS2C;
import com.political.world.structures.StructureManager;
import com.political.world.structures.StructureSite;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Original "Echo Archive" artefacts — a small curated set of lore-forward utility items.
 */
public final class EchoItems {

    public static final String MOD_ID = "politicalserver";

    public static Item VEILSTONE_LENS;
    public static Item RESONANCE_AMPOULE;
    public static Item GLASSBOUND_CODEX;
    public static Item MNEMONIC_SEAL;

    private static final Map<String, Item> ALL = new LinkedHashMap<>();

    private EchoItems() {}

    public static List<Item> items() {
        return new ArrayList<>(ALL.values());
    }

    public static void register() {
        VEILSTONE_LENS = reg("echo_veilstone_lens", "Veilstone Lens",
                "Resonates toward the nearest recorded structure or village.");
        RESONANCE_AMPOULE = reg("echo_resonance_ampoule", "Resonance Ampoule",
                "Doubles cursed-energy recovery for 45 seconds.");
        GLASSBOUND_CODEX = reg("echo_glassbound_codex", "Glassbound Codex",
                "Opens the Echo Archive — fragments of cursed history.");
        MNEMONIC_SEAL = reg("echo_mnemonic_seal", "Mnemonic Seal",
                "Imprints your current position into the seal's memory.");

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            ItemStack stack = sp.getItemInHand(hand);
            if (stack.is(VEILSTONE_LENS)) return consume(sp, stack, useVeilstone(sp));
            if (stack.is(RESONANCE_AMPOULE)) return consume(sp, stack, useAmpoule(sp));
            if (stack.is(GLASSBOUND_CODEX)) return useCodex(sp, stack);
            if (stack.is(MNEMONIC_SEAL)) return useMnemonic(sp, stack);
            return InteractionResult.PASS;
        });
    }

    private static InteractionResult useCodex(ServerPlayer sp, ItemStack stack) {
        com.political.net.ModNetworking.send(sp, new EchoCodexOpenS2C(0));
        return InteractionResult.SUCCESS;
    }

    private static boolean useVeilstone(ServerPlayer sp) {
        if (!(sp.level() instanceof ServerLevel level)) return false;
        StructureSite site = StructureManager.nearest(level, sp.getX(), sp.getZ());
        if (site != null) {
            pointTo(sp, site.x, site.z, site.type.display + " (" + site.type.id + ")");
            return true;
        }
        TagKey<Structure> village = TagKey.create(Registries.STRUCTURE,
                Identifier.fromNamespaceAndPath("minecraft", "village"));
        BlockPos found = level.findNearestMapStructure(village, sp.blockPosition(), 6400, false);
        if (found != null) {
            pointTo(sp, found.getX(), found.getZ(), "nearest village");
            return true;
        }
        msg(sp, "The lens finds only silence — no structures within range.", ChatFormatting.GRAY);
        return false;
    }

    private static void pointTo(ServerPlayer sp, int tx, int tz, String label) {
        double dx = tx - sp.getX();
        double dz = tz - sp.getZ();
        int dist = (int) Math.sqrt(dx * dx + dz * dz);
        String dir = compass(dx, dz);
        msg(sp, "Veilstone trembles toward " + label + ": " + dir + " (~" + dist + "m).", ChatFormatting.AQUA);
    }

    private static String compass(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (angle < 0) angle += 360;
        String[] dirs = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        return dirs[(int) Math.round(angle / 45.0) % 8];
    }

    private static boolean useAmpoule(ServerPlayer sp) {
        if (StatManager.getMaxCursedEnergy(sp) <= 0) {
            msg(sp, "You carry no cursed energy to resonate.", ChatFormatting.GRAY);
            return false;
        }
        StatManager.addCursedEnergy(sp, StatManager.getMaxCursedEnergy(sp) * 0.55);
        msg(sp, "Resonance floods your pathways.", ChatFormatting.LIGHT_PURPLE);
        return true;
    }

    private static InteractionResult useMnemonic(ServerPlayer sp, ItemStack stack) {
        BlockPos pos = sp.blockPosition();
        String label = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Mnemonic Seal \u00BB " + label)
                .withStyle(ChatFormatting.GOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Imprinted at " + label).withStyle(ChatFormatting.GRAY),
                Component.literal("Dimension: " + sp.level().dimension().identifier()).withStyle(ChatFormatting.DARK_GRAY)
        )));
        msg(sp, "Position imprinted into the seal.", ChatFormatting.GOLD);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult consume(ServerPlayer sp, ItemStack stack, boolean ok) {
        if (!ok) return InteractionResult.FAIL;
        if (!sp.isCreative()) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private static void msg(ServerPlayer sp, String text, ChatFormatting color) {
        sp.sendSystemMessage(Component.literal(text).withStyle(color));
    }

    private static Item reg(String name, String title, String desc) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(new Item.Properties().stacksTo(name.contains("seal") ? 1 : 16).setId(key));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        ALL.put(name, registered);
        return registered;
    }

    public static ItemStack display(Item item, String desc) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(desc).withStyle(ChatFormatting.GRAY),
                Component.literal("Echo Archive").withStyle(ChatFormatting.DARK_PURPLE))));
        return stack;
    }
}
