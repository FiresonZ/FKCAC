package com.fkcac.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * S -> C, discriminator 9, "CatAntiCheat" channel.
 * Periodic/vanilla data re-check trigger. The real client re-runs the vanilla check
 * and re-reports any injected classes.
 */
public class SPacketDataCheck implements IMessage {

    public SPacketDataCheck() { }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) { // server -> client only
    }
}