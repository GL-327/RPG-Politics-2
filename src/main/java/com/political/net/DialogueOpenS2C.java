package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record DialogueOpenS2C(UUID villagerId, String villagerName, String role,
                              String nodeId, String line, List<DialogueChoice> choices) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DialogueOpenS2C> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath("politicalserver", "dialogue_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueOpenS2C> CODEC = StreamCodec.of(
            (buf, payload) -> payload.write(buf),
            DialogueOpenS2C::read);

    public static DialogueOpenS2C from(UUID villagerId, String name, String role, String nodeId,
                                       String line, List<DialogueChoice> choices) {
        return new DialogueOpenS2C(villagerId, name, role, nodeId, line, choices);
    }

    private static DialogueOpenS2C read(RegistryFriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        String name = buf.readUtf();
        String role = buf.readUtf();
        String nodeId = buf.readUtf();
        String line = buf.readUtf();
        int n = buf.readVarInt();
        List<DialogueChoice> choices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            choices.add(new DialogueChoice(buf.readUtf(), buf.readUtf(), buf.readUtf()));
        }
        return new DialogueOpenS2C(id, name, role, nodeId, line, choices);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(villagerId);
        buf.writeUtf(villagerName);
        buf.writeUtf(role);
        buf.writeUtf(nodeId);
        buf.writeUtf(line);
        buf.writeVarInt(choices.size());
        for (DialogueChoice c : choices) {
            buf.writeUtf(c.id());
            buf.writeUtf(c.label());
            buf.writeUtf(c.action());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record DialogueChoice(String id, String label, String action) {}
}
