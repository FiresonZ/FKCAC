package com.fkcac.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * C -> S, discriminator 11, "CatAntiCheat" channel.
 * Sent immediately after CPacketHelloReply to announce the client identity for auth.
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
        ByteBufUtils.writeUTF8String(buf, clientId);
    }
}
