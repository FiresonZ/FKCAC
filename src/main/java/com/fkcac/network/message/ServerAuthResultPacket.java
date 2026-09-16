package com.fkcac.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * S -> C, discriminator 14, "CatAntiCheat" channel.
 * Server replies with whether the client is authorized and an optional result string.
 */
public class ServerAuthResultPacket implements IMessage {
    public boolean authorized;
    public String result;

    public ServerAuthResultPacket() { }

    @Override
    public void fromBytes(ByteBuf buf) {
        authorized = buf.readBoolean();
        result = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }
}
