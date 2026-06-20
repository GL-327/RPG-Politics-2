package com.political.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Periodic remaining-time update for the active Court Domain. */
public record CourtTimerS2C(int remainingSeconds) implements CustomPayload {

    public static final CustomPayload.Id<CourtTimerS2C> ID =
            new CustomPayload.Id<>(Identifier.of("politicalserver", "court_timer"));

    public static final PacketCodec<RegistryByteBuf, CourtTimerS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, CourtTimerS2C::remainingSeconds,
            CourtTimerS2C::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
