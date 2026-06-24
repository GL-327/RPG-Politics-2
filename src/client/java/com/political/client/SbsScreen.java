package com.political.client;

import com.political.items.ItemActiveAbility;
import com.political.items.Rarity;
import com.political.items.Variant;
import com.political.net.SbsApplyC2S;
import com.political.net.SbsOpenS2C;
import com.political.power.Power;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * The {@code /sbs} (Skyblock-stats) editor: a sleek, dark, server-authoritative tool for editing the
 * held item's stats, rarity and prefix. Stats are stepped with {@code −}/{@code +} (hold SHIFT ×5,
 * CTRL ×25); rarity, prefix and the prefix-bound ability are cycled with one-click pills. A live
 * preview shows the item name in its rarity colour, and a moving accent shimmer keeps it feeling alive.
 *
 * <p>Nothing is changed on the server until {@code Apply} is pressed (sends {@link SbsApplyC2S}); the
 * server then re-decorates the item and resends a fresh {@link SbsOpenS2C} snapshot to refresh us.
 */
public class SbsScreen extends RpgScreen {

    private static final int PANEL_W = 392;
    private static final int PANEL_H = 256;

    /** The open instance, so a server resend refreshes it in place (no Minecraft.screen in 26.2). */
    public static SbsScreen OPEN;

    // Stat layout (index order mirrors SbsApplyC2S).
    private static final String[] STAT_LABELS = {
            "Health", "Defense", "Strength", "Mana \u00b7 Int", "Damage",
            "Crit Chance", "Crit Damage", "Ferocity", "Speed", "Atk Speed"
    };
    private static final int[] STAT_STEP = {5, 5, 5, 5, 5, 1, 5, 5, 1, 1};
    private static final int[] STAT_MAX = {
            100000, 100000, 100000, 100000, 100000,
            100, 100000, 100000, 10000, 1000
    };
    private static final String[] STAT_SUFFIX = {"", "", "", "", "", "%", "%", "", "", ""};

    private static final List<Power> CURSED_POWERS = Power.ofOrigin(Power.Origin.CURSED_TECHNIQUE);
    private static final ItemActiveAbility[] ABILITIES = ItemActiveAbility.values();

    private final long openedAt = System.currentTimeMillis();

    private boolean hasItem;
    private String itemName = "";
    private final int[] stats = new int[10];
    private int rarityIdx;
    private int variantIdx;
    private int cursedGrade = 1;
    private int powerIdx;
    private int abilityIdx;

    private int left, top;

    public SbsScreen(SbsOpenS2C payload) {
        super(Component.literal("Skyblock Stats Editor"));
        OPEN = this;
        readPayload(payload);
    }

    /** Refreshes the editor from a server snapshot (e.g. after an Apply) without losing the screen. */
    public void apply(SbsOpenS2C payload) {
        readPayload(payload);
        rebuild();
    }

    private void readPayload(SbsOpenS2C p) {
        hasItem = p.hasItem();
        itemName = p.itemName();
        stats[0] = p.health();
        stats[1] = p.defense();
        stats[2] = p.strength();
        stats[3] = p.intelligence();
        stats[4] = p.damage();
        stats[5] = p.critChance();
        stats[6] = p.critDamage();
        stats[7] = p.ferocity();
        stats[8] = p.speed();
        stats[9] = p.attackSpeed();
        rarityIdx = Math.max(0, indexOf(Rarity.values(), p.rarity()));
        variantIdx = Math.max(0, indexOf(Variant.values(), p.variant()));
        cursedGrade = Math.max(1, Math.min(4, p.cursedGrade() == 0 ? 1 : p.cursedGrade()));
        powerIdx = Math.max(0, cursedPowerIndex(p.prefixPower()));
        abilityIdx = Math.max(0, abilityIndex(p.prefixAbility()));
    }

    private static int indexOf(Enum<?>[] values, String name) {
        for (int i = 0; i < values.length; i++) if (values[i].name().equalsIgnoreCase(name)) return i;
        return 0;
    }

    private int cursedPowerIndex(String powerId) {
        for (int i = 0; i < CURSED_POWERS.size(); i++) {
            if (CURSED_POWERS.get(i).id().equalsIgnoreCase(powerId)) return i;
        }
        return 0;
    }

    private int abilityIndex(String abilityId) {
        for (int i = 0; i < ABILITIES.length; i++) if (ABILITIES[i].name().equalsIgnoreCase(abilityId)) return i;
        return 0;
    }

    @Override
    public void removed() {
        if (OPEN == this) OPEN = null;
        super.removed();
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        buildWidgets();
    }

    private void rebuild() {
        clearWidgets();
        buildWidgets();
    }

    private void buildWidgets() {
        // Always-present footer controls.
        int footerY = top + PANEL_H - 26;
        if (hasItem) {
            addRenderableWidget(Button.builder(Component.literal("\u2714 Apply"), b -> sendApply())
                    .bounds(left + 12, footerY, 110, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Reset"), b -> requestRefresh())
                    .bounds(left + 128, footerY, 70, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(left + PANEL_W - 84, footerY, 72, 20).build());

        if (!hasItem) return;

        // ---- Left column: stat steppers ----
        int sx = left + 14;
        int rowY = top + 50;
        for (int i = 0; i < STAT_LABELS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal("\u2212"), b -> step(idx, -1))
                    .bounds(sx + 104, rowY, 16, 14).build());
            addRenderableWidget(Button.builder(Component.literal("+"), b -> step(idx, +1))
                    .bounds(sx + 154, rowY, 16, 14).build());
            rowY += 18;
        }

        // ---- Right column: rarity / prefix / ability ----
        int rx = left + 206;
        int ry = top + 50;
        int rw = PANEL_W - 206 - 14;

        addRenderableWidget(Button.builder(rarityLabel(), b -> {
            rarityIdx = (rarityIdx + 1) % Rarity.values().length;
            b.setMessage(rarityLabel());
        }).bounds(rx, ry, rw, 18).build());
        ry += 24;

        addRenderableWidget(Button.builder(variantLabel(), b -> {
            variantIdx = (variantIdx + 1) % Variant.values().length;
            rebuild();
        }).bounds(rx, ry, rw, 18).build());
        ry += 24;

        Variant variant = Variant.values()[variantIdx];
        if (variant == Variant.CURSED) {
            addRenderableWidget(Button.builder(Component.literal("Grade \u2212"), b -> {
                cursedGrade = Math.max(1, cursedGrade - 1);
                b.setMessage(Component.literal("Grade " + cursedGrade + "  \u2212"));
            }).bounds(rx, ry, rw / 2 - 2, 18).build());
            addRenderableWidget(Button.builder(Component.literal("Grade +"), b -> {
                cursedGrade = Math.min(4, cursedGrade + 1);
            }).bounds(rx + rw / 2 + 2, ry, rw / 2 - 2, 18).build());
            ry += 24;
            if (!CURSED_POWERS.isEmpty()) {
                addRenderableWidget(Button.builder(powerLabel(), b -> {
                    powerIdx = (powerIdx + 1) % CURSED_POWERS.size();
                    b.setMessage(powerLabel());
                }).bounds(rx, ry, rw, 18).build());
            }
        } else if (variant == Variant.UNIQUE) {
            if (ABILITIES.length > 0) {
                addRenderableWidget(Button.builder(abilityLabel(), b -> {
                    abilityIdx = (abilityIdx + 1) % ABILITIES.length;
                    b.setMessage(abilityLabel());
                }).bounds(rx, ry, rw, 18).build());
            }
        }
    }

    private Component rarityLabel() {
        Rarity r = Rarity.values()[rarityIdx];
        return Component.literal("Rarity: " + r.display).withStyle(r.color);
    }

    private Component variantLabel() {
        Variant v = Variant.values()[variantIdx];
        String name = v == Variant.NONE ? "None" : v.display;
        Component c = Component.literal("Prefix: " + name);
        return v.color == null ? c : c.copy().withStyle(v.color);
    }

    private Component powerLabel() {
        Power p = CURSED_POWERS.get(powerIdx);
        return Component.literal("\u2620 " + p.displayName).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    private Component abilityLabel() {
        return Component.literal("\u2726 " + ABILITIES[abilityIdx].displayName).withStyle(ChatFormatting.GOLD);
    }

    private void step(int idx, int dir) {
        int mult = ctrlDown() ? 25 : shiftDown() ? 5 : 1;
        int next = stats[idx] + dir * STAT_STEP[idx] * mult;
        stats[idx] = Math.max(0, Math.min(STAT_MAX[idx], next));
    }

    private static boolean shiftDown() {
        var h = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(h, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(h, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private static boolean ctrlDown() {
        var h = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(h, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(h, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private void sendApply() {
        Variant variant = Variant.values()[variantIdx];
        String prefixPower = variant == Variant.CURSED && !CURSED_POWERS.isEmpty()
                ? CURSED_POWERS.get(powerIdx).id() : "";
        String prefixAbility = variant == Variant.UNIQUE && ABILITIES.length > 0
                ? ABILITIES[abilityIdx].name() : "";
        ClientPlayNetworking.send(new SbsApplyC2S(
                stats[0], stats[1], stats[2], stats[3], stats[4],
                stats[5], stats[6], stats[7], stats[8], stats[9],
                Rarity.values()[rarityIdx].name(), variant.name(), cursedGrade,
                prefixPower, prefixAbility));
    }

    /** Re-opens the editor from the server to discard local edits. */
    private void requestRefresh() {
        // The server resends a snapshot whenever /sbs runs; here we simply re-read the last snapshot.
        rebuild();
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractBackground(g, mouseX, mouseY, pt);
        glassBackdrop(g);
        panel(g, left, top, PANEL_W, PANEL_H);
        headerBar(g, left, top, PANEL_W, 24);

        // Subtle animated shimmer travelling along the header accent line.
        float t = ((System.currentTimeMillis() - openedAt) % 2600L) / 2600f;
        int shimmerX = left + (int) (t * PANEL_W);
        int sw = 46;
        g.fillGradient(Math.max(left, shimmerX - sw), top + 24, Math.min(left + PANEL_W, shimmerX), top + 25,
                0x00FFFFFF, 0x88BFE6FF);
        g.fillGradient(shimmerX, top + 24, Math.min(left + PANEL_W, shimmerX + sw), top + 25,
                0x88BFE6FF, 0x00FFFFFF);

        if (hasItem) {
            inset(g, left + 10, top + 44, 184, 184);   // stat column backing
            inset(g, left + 202, top + 44, PANEL_W - 212, 120); // prefix column backing
            inset(g, left + 202, top + 170, PANEL_W - 212, 40); // preview backing
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);

        // Gently pulsing title.
        float pulse = 0.5f + 0.5f * (float) Math.sin((System.currentTimeMillis() - openedAt) / 600.0);
        int titleCol = lerpColor(COL_ACCENT, COL_GOLD, pulse);
        centered(g, Component.literal("\u2726 SKYBLOCK STATS EDITOR").withStyle(ChatFormatting.BOLD),
                left + PANEL_W / 2, top + 8, titleCol);

        if (!hasItem) {
            centered(g, Component.literal("Hold an item to edit its Skyblock stats."),
                    left + PANEL_W / 2, top + PANEL_H / 2 - 8, COL_TEXT);
            return;
        }

        // Stat rows: label (left) + value (right of label, before the steppers).
        int sx = left + 14;
        int rowY = top + 50;
        for (int i = 0; i < STAT_LABELS.length; i++) {
            label(g, STAT_LABELS[i], sx, rowY + 3, COL_TEXT);
            String val = "+" + stats[i] + STAT_SUFFIX[i];
            int vw = font.width(val);
            g.text(font, val, sx + 102 - vw, rowY + 3, statColor(i), true);
            rowY += 18;
        }

        // Prefix column header.
        label(g, "RARITY \u00b7 PREFIX \u00b7 ABILITY", left + 206, top + 36, COL_TEXT_DIM);

        // Live preview of the resulting item name in its rarity colour.
        Rarity rarity = Rarity.values()[rarityIdx];
        Variant variant = Variant.values()[variantIdx];
        String prefix = variant == Variant.NONE ? "" : variant.display + " ";
        label(g, "Preview", left + 208, top + 174, COL_TEXT_DIM);
        label(g, Component.literal(prefix + stripPrefix(itemName)).withStyle(rarity.color, ChatFormatting.BOLD),
                left + 208, top + 188, 0xFFFFFFFF);

        // Hint footer.
        label(g, "SHIFT \u00d75   CTRL \u00d725", sx, top + PANEL_H - 40, COL_TEXT_DIM);
    }

    /** Removes any existing "Cursed "/"Unique " prefix so the preview doesn't double it up. */
    private static String stripPrefix(String name) {
        for (Variant v : Variant.values()) {
            if (v != Variant.NONE && name.startsWith(v.display + " ")) {
                return name.substring(v.display.length() + 1);
            }
        }
        return name;
    }

    private int statColor(int i) {
        return switch (i) {
            case 0 -> 0xFFFF6B6B; // health
            case 1 -> 0xFF7CE0A0; // defense
            case 2, 4, 6, 7 -> 0xFFFF8A5C; // strength/damage/critdmg/ferocity
            case 3 -> 0xFF5AD2FF; // mana
            case 5 -> 0xFF6FB7FF; // crit chance
            default -> COL_TEXT;
        };
    }

    private static int lerpColor(int a, int b, float t) {
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int ca = (int) (aa + (ba - aa) * t);
        int cr = (int) (ar + (br - ar) * t);
        int cg = (int) (ag + (bg - ag) * t);
        int cb = (int) (ab + (bb - ab) * t);
        return (ca << 24) | (cr << 16) | (cg << 8) | cb;
    }
}
