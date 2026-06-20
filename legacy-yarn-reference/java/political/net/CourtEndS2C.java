package com.political.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Tells clients the Court Domain has closed and the trial overlay should clear. */
public record CourtEndS2C() implements CustomPayload {

    public static final CourtEndS2C INSTANCE = new CourtEndS2C();

    public static final CustomPayload.Id<CourtEndS2C> ID =
            new CustomPayload.Id<>(Identifier.of("politicalserver", "court_end"));

    public static final PacketCodec<RegistryByteBuf, CourtEndS2C> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
