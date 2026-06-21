package com.political.guide;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server -> client: open the Field Manual encyclopedia at the given chapter index. */
public record GuideOpenS2C(int chapter) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GuideOpenS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "guide_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GuideOpenS2C> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, GuideOpenS2C::chapter,
            GuideOpenS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
