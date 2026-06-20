package com.political.client;

import com.political.dev.DevConfigKey;
import com.political.net.DevConfigSetC2S;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * The Developer Menu: a custom config screen for every tunable in the mod. Built entirely
 * from self-rendering buttons (toggle for flags, -/+ for numbers) so it works without any
 * direct text-draw calls. Every change is sent to the server immediately.
 */
public class DevMenuScreen extends Screen {

    private final float[] values;
    private final Button[] valueButtons;

    public DevMenuScreen(List<Float> initial) {
        super(Component.literal("RPG Politics \u2014 Developer Menu"));
        DevConfigKey[] keys = DevConfigKey.values();
        this.values = new float[keys.length];
        this.valueButtons = new Button[keys.length];
        for (int i = 0; i < keys.length; i++) {
            this.values[i] = (i < initial.size()) ? initial.get(i) : DevConfigKey.values()[i].min;
        }
    }

    @Override
    protected void init() {
        DevConfigKey[] keys = DevConfigKey.values();
        int half = (keys.length + 1) / 2;
        int colGap = 12;
        int colWidth = 168;
        int leftX = this.width / 2 - colWidth - colGap / 2;
        int rightX = this.width / 2 + colGap / 2;

        for (int i = 0; i < keys.length; i++) {
            boolean leftCol = i < half;
            int colX = leftCol ? leftX : rightX;
            int row = leftCol ? i : i - half;
            int y = 36 + row * 24;
            DevConfigKey key = keys[i];

            if (key.isBool) {
                final int idx = i;
                valueButtons[i] = Button.builder(boolLabel(key), b -> {
                    values[idx] = values[idx] >= 0.5f ? 0f : 1f;
                    push(keys[idx]);
                    b.setMessage(boolLabel(keys[idx]));
                }).bounds(colX, y, colWidth, 20).build();
                addRenderableWidget(valueButtons[i]);
            } else {
                final int idx = i;
                int btnW = 22;
                int labelW = colWidth - 2 * btnW - 4;
                addRenderableWidget(Button.builder(Component.literal("\u2212"), b -> step(idx, -1))
                        .bounds(colX, y, btnW, 20).build());
                valueButtons[i] = Button.builder(numberLabel(key, values[i]), b -> {})
                        .bounds(colX + btnW + 2, y, labelW, 20).build();
                valueButtons[i].active = false;
                addRenderableWidget(valueButtons[i]);
                addRenderableWidget(Button.builder(Component.literal("+"), b -> step(idx, 1))
                        .bounds(colX + btnW + 2 + labelW + 2, y, btnW, 20).build());
            }
        }

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(this.width / 2 - 60, 36 + half * 24 + 8, 120, 20).build());
    }

    private void step(int idx, int dir) {
        DevConfigKey key = DevConfigKey.values()[idx];
        values[idx] = key.clamp(values[idx] + dir * key.step);
        push(key);
        valueButtons[idx].setMessage(numberLabel(key, values[idx]));
    }

    private void push(DevConfigKey key) {
        ClientPlayNetworking.send(new DevConfigSetC2S(key.name(), values[key.ordinal()]));
    }

    private Component boolLabel(DevConfigKey key) {
        boolean on = values[key.ordinal()] >= 0.5f;
        return Component.literal(key.label + ": " + (on ? "ON" : "OFF"));
    }

    private Component numberLabel(DevConfigKey key, float v) {
        String s = (key.step >= 1f) ? String.valueOf(Math.round(v)) : String.format("%.3f", v);
        return Component.literal(key.label + ": " + s);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
