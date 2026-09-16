package com.fkcac.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * C -> S, discriminator 10, "CatAntiCheat" channel.
 * Reports two vanilla-renderer flags (smooth lighting enabled, transparent texture pack)
 * that the server uses to whitelist clients running on a non-vanilla render pipeline.
 */
public class CPacketVanillaData implements IMessage {
    private final boolean lighting;
    private final boolean transparentTexture;

    public CPacketVanillaData() {
        this(true, true);
    }

    public CPacketVanillaData(boolean lighting, boolean transparentTexture) {
        this.lighting = lighting;
        this.transparentTexture = transparentTexture;
    }

    @Override
    public void fromBytes(ByteBuf buf) { // client -> server only
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(lighting);
        buf.writeBoolean(transparentTexture);
    }
}