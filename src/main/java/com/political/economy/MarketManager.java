package com.political.economy;

import com.political.politics.DataManager;
import com.political.politics.PoliticsData;
import net.minecraft.server.MinecraftServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Simulated crypto + stock markets. Prices live in {@link PoliticsData} (so they
 * persist across restarts) and drift via a random walk nudged by player trades.
 */
public final class MarketManager {

    public enum Category { CRYPTO, STOCK }

    /** symbol -> display name; also the seed list. */
    public static final Map<String, String> CRYPTO = new LinkedHashMap<>();
    public static final Map<String, String> STOCK = new LinkedHashMap<>();
    private static final Map<String, Double> CRYPTO_SEED = new LinkedHashMap<>();
    private static final Map<String, Double> STOCK_SEED = new LinkedHashMap<>();

    static {
        seedCrypto("END", "Endercoin", 45000);
        seedCrypto("NTH", "Nethercoin", 8200);
        seedCrypto("DOGE", "Dogecoin", 12);
        seedCrypto("CHNK", "Chunkcoin", 340);
        seedCrypto("WRDN", "Wardcoin", 1500);
        seedCrypto("EUSD", "Ender Stablecoin", 1000);

        seedStock("MOJ", "Mojang Industries", 2600);
        seedStock("REDS", "Redstone Logic Co.", 480);
        seedStock("CARTO", "Cartographer Corp", 150);
        seedStock("NETH", "Nether Refineries", 920);
        seedStock("AQUA", "Aqua Holdings", 210);
        seedStock("VLLG", "Villager Trade Union", 75);
    }

    private static final long TICK_INTERVAL = 1200L; // ~1 minute
    private static final Random RNG = new Random();
    private static long counter = 0;

    private MarketManager() {}

    private static void seedCrypto(String sym, String name, double price) {
        CRYPTO.put(sym, name);
        CRYPTO_SEED.put(sym, price);
    }

    private static void seedStock(String sym, String name, double price) {
        STOCK.put(sym, name);
        STOCK_SEED.put(sym, price);
    }

    public static Map<String, String> names(Category cat) {
        return cat == Category.CRYPTO ? CRYPTO : STOCK;
    }

    private static Map<String, Double> prices(Category cat) {
        PoliticsData d = DataManager.data();
        return cat == Category.CRYPTO ? d.cryptoPrices : d.stockPrices;
    }

    private static Map<String, Long> holdings(Category cat) {
        PoliticsData d = DataManager.data();
        return cat == Category.CRYPTO ? d.cryptoHoldings : d.stockHoldings;
    }

    public static void ensureSeeded() {
        seedInto(Category.CRYPTO, CRYPTO_SEED);
        seedInto(Category.STOCK, STOCK_SEED);
    }

    private static void seedInto(Category cat, Map<String, Double> seed) {
        Map<String, Double> live = prices(cat);
        for (var e : seed.entrySet()) live.putIfAbsent(e.getKey(), e.getValue());
    }

    public static double price(Category cat, String symbol) {
        ensureSeeded();
        return prices(cat).getOrDefault(symbol.toUpperCase(), 0.0);
    }

    public static boolean isValid(Category cat, String symbol) {
        return names(cat).containsKey(symbol.toUpperCase());
    }

    public static long held(Category cat, String uuid, String symbol) {
        return holdings(cat).getOrDefault(uuid + "|" + symbol.toUpperCase(), 0L);
    }

    /** Returns coins spent, or -1 if insufficient funds. */
    public static long buy(Category cat, String uuid, String symbol, long units) {
        symbol = symbol.toUpperCase();
        long cost = (long) Math.ceil(price(cat, symbol) * units);
        if (!DataManager.removeCoins(uuid, (int) Math.min(Integer.MAX_VALUE, cost))) return -1;
        holdings(cat).merge(uuid + "|" + symbol, units, Long::sum);
        nudge(cat, symbol, 1.002);
        return cost;
    }

    /** Returns coins earned, or -1 if the player doesn't hold that many units. */
    public static long sell(Category cat, String uuid, String symbol, long units) {
        symbol = symbol.toUpperCase();
        String key = uuid + "|" + symbol;
        long have = holdings(cat).getOrDefault(key, 0L);
        if (have < units) return -1;
        long gain = (long) Math.floor(price(cat, symbol) * units);
        if (have - units <= 0) holdings(cat).remove(key);
        else holdings(cat).put(key, have - units);
        DataManager.addCoins(uuid, (int) Math.min(Integer.MAX_VALUE, gain));
        nudge(cat, symbol, 0.998);
        return gain;
    }

    private static void nudge(Category cat, String symbol, double factor) {
        Map<String, Double> live = prices(cat);
        Double cur = live.get(symbol);
        if (cur != null) live.put(symbol, cur * factor);
    }

    public static void tick(MinecraftServer server) {
        if (++counter % TICK_INTERVAL != 0) return;
        ensureSeeded();
        walk(Category.CRYPTO, 0.08);
        walk(Category.STOCK, 0.03);
        DataManager.data().lastMarketTick = System.currentTimeMillis();
    }

    private static void walk(Category cat, double volatility) {
        Map<String, Double> live = prices(cat);
        Map<String, Double> seed = cat == Category.CRYPTO ? CRYPTO_SEED : STOCK_SEED;
        for (var e : live.entrySet()) {
            double change = RNG.nextGaussian() * volatility;
            double next = e.getValue() * (1.0 + change);
            double floor = seed.getOrDefault(e.getKey(), 1.0) * 0.1;
            e.setValue(Math.max(floor, next));
        }
    }
}
