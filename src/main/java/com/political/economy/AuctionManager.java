package com.political.economy;

import com.political.politics.DataManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Player-to-player auction house. Listings hold the live ItemStack in memory
 * (not persisted across restarts in this build, to avoid item NBT serialization).
 *
 * <p>Polish in Expansion 3: a listing fee feeds the treasury (waived under the
 * Free Market law), sellers may cancel and reclaim listings, and stale listings
 * expire automatically, returning goods to online sellers.
 */
public final class AuctionManager {

    public record Listing(int id, String sellerUuid, String sellerName, ItemStack stack, int price, long createdAt) {}

    private static final List<Listing> LISTINGS = new ArrayList<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private static final long EXPIRY_MS = 60L * 60 * 1000; // 1 hour
    private static int tickCounter = 0;

    private AuctionManager() {}

    public static List<Listing> listings() {
        return LISTINGS;
    }

    /** Listing fee: 2% of asking price (min 5), waived while the Free Market law is in force. */
    public static int listingFee(int price) {
        if (com.political.civics.LawManager.isActive(com.political.civics.CivicLaw.FREE_MARKET)) return 0;
        return Math.max(5, (int) Math.ceil(price * 0.02));
    }

    /**
     * Lists the player's main-hand stack. Returns the listing id, -1 if the hand
     * is empty, or -2 if the seller can't afford the listing fee.
     */
    public static int list(ServerPlayer seller, int price) {
        ItemStack held = seller.getMainHandItem();
        if (held.isEmpty()) return -1;
        int fee = listingFee(price);
        if (fee > 0 && !DataManager.removeCoins(seller.getStringUUID(), fee)) return -2;
        if (fee > 0) DataManager.addTreasury(fee);
        Listing listing = new Listing(NEXT_ID.getAndIncrement(), seller.getStringUUID(),
                seller.getName().getString(), held.copy(), price, System.currentTimeMillis());
        LISTINGS.add(listing);
        seller.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return listing.id;
    }

    /** 0 = bought, 1 = not found, 2 = insufficient funds, 3 = own listing. */
    public static int buy(ServerPlayer buyer, int id) {
        Listing target = null;
        for (Listing l : LISTINGS) {
            if (l.id == id) { target = l; break; }
        }
        if (target == null) return 1;
        if (target.sellerUuid.equals(buyer.getStringUUID())) return 3;
        if (!DataManager.removeCoins(buyer.getStringUUID(), target.price)) return 2;
        DataManager.addCoins(target.sellerUuid, target.price);
        ItemStack stack = target.stack.copy();
        if (!buyer.getInventory().add(stack)) {
            buyer.drop(stack, false);
        }
        LISTINGS.remove(target);
        return 0;
    }

    /** Seller reclaims their own listing. 0 = ok, 1 = not found, 3 = not the owner. */
    public static int cancel(ServerPlayer seller, int id) {
        for (Iterator<Listing> it = LISTINGS.iterator(); it.hasNext(); ) {
            Listing l = it.next();
            if (l.id != id) continue;
            if (!l.sellerUuid.equals(seller.getStringUUID())) return 3;
            ItemStack stack = l.stack.copy();
            if (!seller.getInventory().add(stack)) seller.drop(stack, false);
            it.remove();
            return 0;
        }
        return 1;
    }

    public static void tick(MinecraftServer server) {
        if (++tickCounter % 200 != 0 || LISTINGS.isEmpty()) return; // ~every 10s
        long now = System.currentTimeMillis();
        for (Iterator<Listing> it = LISTINGS.iterator(); it.hasNext(); ) {
            Listing l = it.next();
            if (now - l.createdAt() < EXPIRY_MS) continue;
            ServerPlayer seller = server.getPlayerList().getPlayer(java.util.UUID.fromString(l.sellerUuid()));
            if (seller != null) {
                ItemStack stack = l.stack.copy();
                if (!seller.getInventory().add(stack)) seller.drop(stack, false);
                seller.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Your auction #" + l.id() + " expired and was returned."));
            }
            it.remove();
        }
    }
}
