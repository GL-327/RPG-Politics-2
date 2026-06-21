package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client -> server action from the cursed-technique screen / keybinds (JJK overhaul).
 *
 * <p>{@code action} is one of:
 * <ul>
 *   <li>{@code open} &mdash; request a fresh {@link TechniqueMenuS2C}.</li>
 *   <li>{@code cast} &mdash; cast {@code techniqueId} now (spends cursed energy).</li>
 *   <li>{@code cast_slot} &mdash; cast whatever the client has bound to {@code slot} (1..4); the
 *       resolved id travels in {@code techniqueId}.</li>
 * </ul>
 * Slot bindings themselves are kept client-side, so binding never needs a server round-trip.</p>
 */
public record TechniqueActionC2S(String action, String techniqueId, int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TechniqueActionC2S> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "jjk_technique_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TechniqueActionC2S> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeUtf(v.action);
                buf.writeUtf(v.techniqueId);
                buf.writeVarInt(v.slot);
            },
            buf -> new TechniqueActionC2S(buf.readUtf(), buf.readUtf(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
