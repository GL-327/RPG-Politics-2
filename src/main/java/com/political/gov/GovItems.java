package com.political.gov;

import com.political.combat.StatManager;
import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import com.political.politics.Role;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Physical governance items that bring the (formerly chat-only) political layer into the
 * world: a crown for the leader, a gavel for the judge, decrees, ballots and minted money.
 * These were impossible in the old server-side-only mod \u2014 now they are real, tradeable items.
 */
public final class GovItems {

    public static final String MOD_ID = "politicalserver";

    public static Item CROWN, GAVEL, DECREE_SCROLL, BALLOT, TREASURY_NOTE, COIN_POUCH, PASSPORT;
    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    private GovItems() {}

    public static List<Item> items() {
        return new ArrayList<>(ITEMS.values());
    }

    public static void register() {
        CROWN = reg("crown");
        GAVEL = reg("gavel");
        DECREE_SCROLL = reg("decree_scroll");
        BALLOT = reg("ballot");
        TREASURY_NOTE = reg("treasury_note");
        COIN_POUCH = reg("coin_pouch");
        PASSPORT = reg("passport");

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            ItemStack stack = sp.getItemInHand(hand);
            if (stack.is(CROWN)) return crown(sp);
            if (stack.is(DECREE_SCROLL)) return decree(sp);
            if (stack.is(TREASURY_NOTE)) return mint(sp, stack, 250, "Treasury Note");
            if (stack.is(COIN_POUCH)) return mint(sp, stack, 50, "Coin Pouch");
            if (stack.is(PASSPORT)) return passport(sp);
            if (stack.is(GAVEL)) return gavelSelf(sp);
            return InteractionResult.PASS;
        });

        // Gavel on a player -> summon to court; ballot on a player -> vote for them.
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (!(entity instanceof ServerPlayer target)) return InteractionResult.PASS;
            ItemStack stack = sp.getItemInHand(hand);
            if (stack.is(GAVEL)) return summon(sp, target);
            if (stack.is(BALLOT)) return vote(sp, target);
            return InteractionResult.PASS;
        });
    }

    // ---------------- Item behaviours ----------------

    private static InteractionResult crown(ServerPlayer sp) {
        Role role = DataManager.roleOf(sp.getStringUUID());
        if (role == Role.CHAIR || role == Role.DICTATOR) {
            sp.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, false));
            sp.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 1200, 0, false, true));
            broadcast(sp, Component.literal("\uD83D\uDC51 " + sp.getName().getString() + " wears the crown of state!")
                    .withStyle(ChatFormatting.GOLD));
        } else {
            String leader = DataManager.data().chair.isEmpty() ? "no one"
                    : DataManager.nameOf(DataManager.data().chair);
            msg(sp, "The crown belongs to the elected leader: " + leader + ".", ChatFormatting.GOLD);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult decree(ServerPlayer sp) {
        Role role = DataManager.roleOf(sp.getStringUUID());
        if (role != Role.CHAIR && role != Role.DICTATOR) {
            msg(sp, "Only the leader may issue decrees.", ChatFormatting.RED);
            return InteractionResult.SUCCESS;
        }
        broadcast(sp, Component.literal("\uD83D\uDCDC Decree of " + sp.getName().getString()
                + ": let all citizens take heed!").withStyle(ChatFormatting.YELLOW));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult mint(ServerPlayer sp, ItemStack stack, int coins, String label) {
        DataManager.addCoins(sp.getStringUUID(), coins);
        if (!sp.isCreative()) stack.shrink(1);
        msg(sp, "Redeemed a " + label + " for " + coins + " coins.", ChatFormatting.GREEN);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult passport(ServerPlayer sp) {
        String uuid = sp.getStringUUID();
        var trait = DataManager.cursedTrait(uuid);
        msg(sp, "\u2014 Passport \u2014", ChatFormatting.AQUA);
        msg(sp, "Role: " + DataManager.roleOf(uuid).name(), ChatFormatting.GRAY);
        msg(sp, "Cursed aptitude: " + trait.display + " | " + DataManager.gradeLabel(DataManager.sorcererGrade(uuid)), ChatFormatting.GRAY);
        msg(sp, "Coins: " + DataManager.getCoins(uuid) + " | Credits: " + DataManager.getCredits(uuid), ChatFormatting.GRAY);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult gavelSelf(ServerPlayer sp) {
        msg(sp, "Strike a citizen with the gavel to summon them to court.", ChatFormatting.LIGHT_PURPLE);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult summon(ServerPlayer sp, ServerPlayer target) {
        if (!DataManager.isJudge(sp.getUUID())) {
            msg(sp, "Only the Judge may wield the gavel.", ChatFormatting.RED);
            return InteractionResult.SUCCESS;
        }
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0, false, false));
        broadcast(sp, Component.literal("\u2696 " + target.getName().getString()
                + " has been summoned to court by the Judge!").withStyle(ChatFormatting.LIGHT_PURPLE));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult vote(ServerPlayer sp, ServerPlayer target) {
        PoliticsData d = DataManager.data();
        if (!d.electionActive) {
            msg(sp, "There is no active election.", ChatFormatting.RED);
            return InteractionResult.SUCCESS;
        }
        String voter = sp.getStringUUID();
        String cand = target.getStringUUID();
        if (!d.candidates.contains(cand)) {
            msg(sp, target.getName().getString() + " is not a candidate.", ChatFormatting.RED);
            return InteractionResult.SUCCESS;
        }
        if (d.votedPlayers.containsKey(voter)) {
            msg(sp, "You have already cast your ballot.", ChatFormatting.RED);
            return InteractionResult.SUCCESS;
        }
        d.votedPlayers.put(voter, cand);
        d.votes.merge(cand, 1, Integer::sum);
        msg(sp, "Ballot cast for " + target.getName().getString() + ".", ChatFormatting.GREEN);
        return InteractionResult.SUCCESS;
    }

    // ---------------- helpers ----------------

    private static void broadcast(ServerPlayer sp, Component msg) {
        var server = sp.level().getServer();
        if (server != null) server.getPlayerList().broadcastSystemMessage(msg, false);
    }

    private static void msg(ServerPlayer sp, String text, ChatFormatting color) {
        sp.sendSystemMessage(Component.literal(text).withStyle(color));
    }

    private static Item reg(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(new Item.Properties().stacksTo(16).setId(key));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(name, registered);
        return registered;
    }
}
