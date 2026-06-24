package com.political.guide;

import com.political.client.RpgScreen;
import com.political.guide.GuideContent.Block;
import com.political.guide.GuideContent.Chapter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The in-game encyclopedia: a paginated field manual with a chapter sidebar and a scrolling
 * (page-flipped) content area. Built on the mod's shared {@link RpgScreen} 26.2 render pipeline
 * \u2014 no mixins, no textures. Content comes from {@link GuideContent}.
 */
public class GuideScreen extends RpgScreen {

    /** The currently open instance (no Minecraft.screen accessor in 26.2). */
    public static GuideScreen OPEN;

    private static final int PANEL_W = 380;
    private static final int PANEL_H = 240;
    private static final int SIDEBAR_W = 104;
    private static final int LINE_H = 11;

    private final List<Chapter> chapters = GuideContent.chapters();

    private int left, top;
    private int contentLeft, contentTop, contentW, contentH;
    private int linesPerPage;

    private int chapterIdx;
    private int page;

    /** Flattened, wrapped + coloured lines for the active chapter. */
    private final List<Line> lines = new ArrayList<>();
    private Button prevPageBtn, nextPageBtn;

    private record Line(String text, int color) {}

    public GuideScreen(int chapter) {
        super(Component.literal("Field Manual"));
        OPEN = this;
        this.chapterIdx = Math.max(0, Math.min(chapters.size() - 1, chapter));
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

        contentLeft = left + SIDEBAR_W + 12;
        contentTop = top + 30;
        contentW = PANEL_W - SIDEBAR_W - 24;
        contentH = PANEL_H - 30 - 30; // header + footer reserved
        linesPerPage = Math.max(1, contentH / LINE_H);

        // Chapter sidebar.
        int by = top + 28;
        int bx = left + 8;
        for (int i = 0; i < chapters.size(); i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(chapters.get(i).title()), b -> selectChapter(idx))
                    .bounds(bx, by, SIDEBAR_W - 4, 15).build());
            by += 16;
        }

        // Footer: page navigation + close.
        int fy = top + PANEL_H - 24;
        prevPageBtn = Button.builder(Component.literal("\u25C0 Prev"), b -> turnPage(-1))
                .bounds(contentLeft, fy, 58, 18).build();
        nextPageBtn = Button.builder(Component.literal("Next \u25B6"), b -> turnPage(1))
                .bounds(contentLeft + 64, fy, 58, 18).build();
        addRenderableWidget(prevPageBtn);
        addRenderableWidget(nextPageBtn);
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(left + PANEL_W - 60, top + 6, 52, 16).build());

        rebuildLines();
    }

    private void selectChapter(int idx) {
        chapterIdx = idx;
        page = 0;
        rebuildLines();
    }

    private void turnPage(int dir) {
        int pages = pageCount();
        page = Math.max(0, Math.min(pages - 1, page + dir));
        updatePageButtons();
    }

    private int pageCount() {
        return Math.max(1, (lines.size() + linesPerPage - 1) / linesPerPage);
    }

    private void updatePageButtons() {
        if (prevPageBtn != null) prevPageBtn.active = page > 0;
        if (nextPageBtn != null) nextPageBtn.active = page < pageCount() - 1;
    }

    /** Re-wrap the active chapter to the current content width and reset paging. */
    private void rebuildLines() {
        lines.clear();
        Chapter c = chapters.get(chapterIdx);
        for (Block block : c.blocks()) {
            switch (block.style()) {
                case SPACER -> lines.add(new Line("", 0));
                case TITLE -> {
                    for (String s : wrap(block.text(), contentW)) lines.add(new Line(s, COL_GOLD));
                    lines.add(new Line("", 0));
                }
                case HEADER -> {
                    for (String s : wrap(block.text(), contentW)) lines.add(new Line(s, COL_ACCENT));
                }
                case BULLET -> {
                    List<String> wrapped = wrap(block.text(), contentW - 8);
                    for (int i = 0; i < wrapped.size(); i++) {
                        lines.add(new Line((i == 0 ? "\u2022 " : "  ") + wrapped.get(i), COL_TEXT));
                    }
                }
                case NOTE -> {
                    for (String s : wrap(block.text(), contentW)) lines.add(new Line(s, COL_TEXT_DIM));
                }
                default -> {
                    for (String s : wrap(block.text(), contentW)) lines.add(new Line(s, COL_TEXT));
                }
            }
        }
        page = Math.min(page, pageCount() - 1);
        updatePageButtons();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractBackground(g, mouseX, mouseY, pt);
        glassBackdrop(g);
        panel(g, left, top, PANEL_W, PANEL_H);
        headerBar(g, left, top, PANEL_W, 24);
        inset(g, left + 6, top + 28, SIDEBAR_W - 2, PANEL_H - 34);
        inset(g, contentLeft - 6, contentTop - 2, contentW + 12, contentH + 4);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        centered(g, Component.literal("\u2756 FIELD MANUAL").withStyle(ChatFormatting.BOLD),
                left + PANEL_W / 2, top + 8, COL_GOLD);

        // Highlight the active chapter in the sidebar.
        int hy = top + 28 + chapterIdx * 16;
        g.fill(left + 8, hy, left + 8 + SIDEBAR_W - 4, hy + 15, 0x3055AAFF);

        // Content lines for the current page.
        int start = page * linesPerPage;
        int end = Math.min(lines.size(), start + linesPerPage);
        int y = contentTop;
        for (int i = start; i < end; i++) {
            Line ln = lines.get(i);
            if (!ln.text.isEmpty()) label(g, ln.text, contentLeft, y, ln.color);
            y += LINE_H;
        }

        // Page indicator.
        String pg = "Page " + (page + 1) + " / " + pageCount();
        int pw = font.width(pg);
        label(g, pg, contentLeft + contentW - pw, top + PANEL_H - 20, COL_TEXT_DIM);
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isEmpty()) { out.add(""); return out; }
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String trial = line.length() == 0 ? word : line + " " + word;
            if (font.width(trial) > maxWidth && line.length() > 0) {
                out.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(trial);
            }
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }
}
