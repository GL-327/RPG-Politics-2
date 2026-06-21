package com.political.client;

import com.political.net.DialogueChooseC2S;
import com.political.net.DialogueOpenS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/** Fallout-style dialogue screen with numbered response choices and villager voice sounds. */
public class VillagerDialogueScreen extends Screen {

    private final DialogueOpenS2C payload;

    public VillagerDialogueScreen(DialogueOpenS2C payload) {
        super(Component.literal(payload.villagerName()));
        this.payload = payload;
        Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.VILLAGER_AMBIENT, 0.8f));
    }

    @Override
    protected void init() {
        Button title = Button.builder(
                Component.literal(payload.villagerName() + " (" + payload.role() + ")"),
                b -> {}).bounds(width / 2 - 160, height / 2 - 70, 320, 20).build();
        title.active = false;
        addRenderableWidget(title);

        Button line = Button.builder(Component.literal(payload.line()), b -> {})
                .bounds(width / 2 - 160, height / 2 - 45, 320, 40).build();
        line.active = false;
        addRenderableWidget(line);

        int y = height / 2 + 10;
        int i = 1;
        for (DialogueOpenS2C.DialogueChoice c : payload.choices()) {
            final int idx = i++;
            addRenderableWidget(Button.builder(
                    Component.literal(idx + ". " + c.label()),
                    b -> choose(c)).bounds(width / 2 - 150, y, 300, 20).build());
            y += 24;
        }
        if (payload.choices().isEmpty()) {
            addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                    .bounds(width / 2 - 60, y, 120, 20).build());
        }
    }

    private void choose(DialogueOpenS2C.DialogueChoice c) {
        Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.VILLAGER_YES, 1f));
        ClientPlayNetworking.send(new DialogueChooseC2S(c.id(), c.action()));
        if ("farewell".equals(c.action()) || "open_trades".equals(c.action())) {
            onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
