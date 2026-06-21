package com.political.flight;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client -> server report of the player's viltrumite-flight state for the current tick.
 *
 * <p>The actual flight motion is driven client-side (the client is authoritative over its own
 * position), but the server needs to know the throttle so it can apply shared effects: zeroing
 * fall distance, and the high-speed "ramming" knockback that sends anything in the flight path
 * flying. {@code throttle} is the 0..1 acceleration ramp; {@code flying} mirrors the vanilla
 * {@code abilities.flying} toggle so the server can tell airborne players from grounded ones.
 */
public record FlightInputC2S(float throttle, boolean flying) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FlightInputC2S> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "flight_input"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlightInputC2S> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeFloat(v.throttle);
                buf.writeBoolean(v.flying);
            },
            buf -> new FlightInputC2S(buf.readFloat(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
