package com.political.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Tells clients a Court Domain has opened so they can render the trial overlay. */
public record CourtStartS2C(String judgeName, String accusedName, int durationSeconds)
        implements CustomPayload {

    public static final CustomPayload.Id<CourtStartS2C> ID =
            new CustomPayload.Id<>(Identifier.of("politicalserver", "court_start"));

    public static final PacketCodec<RegistryByteBuf, CourtStartS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, CourtStartS2C::judgeName,
            PacketCodecs.STRING, CourtStartS2C::accusedName,
            PacketCodecs.INTEGER, CourtStartS2C::durationSeconds,
            CourtStartS2C::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
