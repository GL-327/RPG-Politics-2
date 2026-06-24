package com.political.client.echo;

import com.political.client.RpgScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Three-chapter lore reader for the Echo Archive — original fiction tied to the mod's cursed-energy world.
 */
public class EchoLoreScreen extends RpgScreen {

    private static final String[][] CHAPTERS = {
            {
                    "The Veilstone Compact",
                    "Before the fracture wars, sorcerers etched lenses from auric quartz — not to see farther,",
                    "but to hear structures breathe. Each lens hums toward places where cursed energy pooled",
                    "long enough to leave a scar in the world. The Archive still collects them, though most",
                    "bear hairline cracks from use."
            },
            {
                    "Resonance Without a Voice",
                    "Cursed energy does not regenerate like mana. It remembers. An ampoule of distilled",
                    "resonance does not create CE — it wakes what sleep inside your pathways, the way a",
                    " struck tuning fork wakes a string. Drink sparingly; the body keeps score."
            },
            {
                    "Mnemonic Seals",
                    "When a domain collapses, the geometry lingers for a heartbeat. Mnemonic seals capture",
                    "that heartbeat — coordinates, dimension, the weight of the air. They are not teleport",
                    "scrolls. They are proof you were there when the world bent."
            }
    };

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 200;

    private int chapter;
    private int left, top;

    public EchoLoreScreen(int chapter) {
        super(Component.literal("Echo Archive"));
        this.chapter = Math.max(0, Math.min(CHAPTERS.length - 1, chapter));
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        addRenderableWidget(Button.builder(Component.literal("\u25C0"), b -> turn(-1))
                .bounds(left + 12, top + PANEL_H - 28, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal("\u25B6"), b -> turn(1))
                .bounds(left + 40, top + PANEL_H - 28, 24, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(left + PANEL_W - 72, top + PANEL_H - 28, 60, 20).build());
    }

    private void turn(int delta) {
        chapter = Math.floorMod(chapter + delta, CHAPTERS.length);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        glassBackdrop(g);
        panel(g, left, top, PANEL_W, PANEL_H);
        headerBar(g, left, top, PANEL_W, 22);
        inset(g, left + 10, top + 30, PANEL_W - 20, PANEL_H - 68);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        centered(g, Component.literal("ECHO ARCHIVE").withStyle(ChatFormatting.BOLD),
                left + PANEL_W / 2, top + 7, COL_ACCENT);
        String[] ch = CHAPTERS[chapter];
        label(g, Component.literal(ch[0]).withStyle(ChatFormatting.GOLD), left + 18, top + 38, COL_GOLD);
        int y = top + 54;
        for (int i = 1; i < ch.length; i++) {
            label(g, ch[i], left + 18, y, COL_TEXT);
            y += 11;
        }
        label(g, (chapter + 1) + " / " + CHAPTERS.length, left + PANEL_W / 2 - 10, top + PANEL_H - 24, COL_TEXT_DIM);
    }
}
