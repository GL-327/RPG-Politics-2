package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client snapshot that opens / refreshes the cursed-technique screen (JJK overhaul).
 *
 * @param grade           the viewer's sorcerer grade (0..5)
 * @param cursedEnergy    current cursed energy
 * @param maxCursedEnergy maximum cursed energy
 * @param known           comma-separated technique ids the viewer has unlocked (by grade)
 * @param bound           comma-separated technique ids bound to slots 1..4 (empty slot = blank token)
 * @param domains         comma-separated domain ids the viewer can expand
 */
public record TechniqueMenuS2C(int grade, float cursedEnergy, float maxCursedEnergy,
                               String known, String bound, String domains) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TechniqueMenuS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "jjk_technique_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TechniqueMenuS2C> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeVarInt(v.grade);
                buf.writeFloat(v.cursedEnergy);
                buf.writeFloat(v.maxCursedEnergy);
                buf.writeUtf(v.known);
                buf.writeUtf(v.bound);
                buf.writeUtf(v.domains);
            },
            buf -> new TechniqueMenuS2C(buf.readVarInt(), buf.readFloat(), buf.readFloat(),
                    buf.readUtf(), buf.readUtf(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
