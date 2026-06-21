package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DialogueChooseC2S(String choiceId, String action) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DialogueChooseC2S> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath("politicalserver", "dialogue_choose"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueChooseC2S> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DialogueChooseC2S::choiceId,
            ByteBufCodecs.STRING_UTF8, DialogueChooseC2S::action,
            DialogueChooseC2S::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
