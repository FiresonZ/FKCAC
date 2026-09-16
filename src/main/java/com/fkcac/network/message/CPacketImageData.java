package com.fkcac.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * C -> S, discriminator 8, "CatAntiCheat" channel.
 * A chunk of screenshot data, up to 32763 bytes. The last chunk carries {@code eof=true}.
 */
public class CPacketImageData implements IMessage {
    private boolean eof;
    private byte[] bytes;

    public CPacketImageData() {
    }

    public CPacketImageData(boolean eof, byte[] bytes) {
        this.eof = eof;
        this.bytes = bytes;
        if (bytes.length > 32763) {
            throw new RuntimeException("Image data size > 32K");
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) { // client -> server only
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(eof);
        buf.writeBytes(bytes);
    }
}