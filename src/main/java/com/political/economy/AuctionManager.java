package com.political.economy;

import com.political.politics.DataManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Player-to-player auction house. Listings hold the live ItemStack in memory
 * (not persisted across restarts in this build, to avoid item NBT serialization).
 */
public final class AuctionManager {

    public record Listing(int id, String sellerUuid, String sellerName, ItemStack stack, int price) {}

    private static final List<Listing> LISTINGS = new ArrayList<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private AuctionManager() {}

    public static List<Listing> listings() {
        return LISTINGS;
    }

    /** Lists the player's main-hand stack. Returns the listing id, or -1 if hand empty. */
    public static int list(ServerPlayer seller, int price) {
        ItemStack held = seller.getMainHandItem();
        if (held.isEmpty()) return -1;
        Listing listing = new Listing(NEXT_ID.getAndIncrement(), seller.getStringUUID(),
                seller.getName().getString(), held.copy(), price);
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
}
