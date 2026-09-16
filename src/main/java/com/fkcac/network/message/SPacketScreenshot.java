package com.fkcac.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * S -> C, discriminator 3, "CatAntiCheat" channel.
 * Server requests a screenshot from the client. The client replies with one or
 * more {@link CPacketImageData} chunks (max 32763 bytes each).
 */
public class SPacketScreenshot implements IMessage {

    public SPacketScreenshot() { }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) { // server -> client only
    }
}