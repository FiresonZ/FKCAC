package com.fkcac.network.message;

import com.fkcac.network.NetUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * C -> S, discriminator 11, "CatAntiCheat" channel.
 * Sent immediately after CPacketHelloReply to announce the client identity for auth.
 * The client id is written as {@code VarInt length + UTF-8 bytes} (see {@link NetUtils}),
 * matching the reference packet (NOT FML's ByteBufUtils).
 */
public class ClientAuthHelloPacket implements IMessage {
    /** Client identity string, always {@code "catanticheat-client"}. */
    private final String clientId;

    public ClientAuthHelloPacket() {
        this("");
    }

    public ClientAuthHelloPacket(String clientId) {
        this.clientId = clientId != null ? clientId : "";
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetUtils.writeString(buf, clientId);
    }
}
