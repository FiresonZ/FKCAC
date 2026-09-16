package com.fkcac.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * S -> C, discriminator 9, "CatAntiCheat" channel.
 * Periodic/vanilla data re-check trigger. Wire format (matches the reference):
 * <pre>
 *   short count + count x UTF8   sampleTextures
 *   boolean                      checkBrightness
 *   float                        transparentThreshold
 *   float                        brightnessThreshold
 *   int                          challengeNonce
 *   5 x (short count + UTF8...)  environmentJvmArgs / SystemProperties / PropertyPatterns
 *                                / ClassLoaders / Threads
 * </pre>
 * All strings use FML {@code ByteBufUtils}. Fields are public so the data-check
 * handler can recompute the vanilla-data response exactly like the original client.
 */
public class SPacketDataCheck implements IMessage {
    public List<String> sampleTextures;
    public boolean checkBrightness;
    public float transparentThreshold;
    public float brightnessThreshold;
    public int challengeNonce;
    public List<String> environmentJvmArgs;
    public List<String> environmentSystemProperties;
    public List<String> environmentPropertyPatterns;
    public List<String> environmentClassLoaders;
    public List<String> environmentThreads;

    public SPacketDataCheck() { }

    @Override
    public void fromBytes(ByteBuf buf) {
        sampleTextures = readStringList(buf);
        checkBrightness = buf.readBoolean();
        transparentThreshold = buf.readFloat();
        brightnessThreshold = buf.readFloat();
        challengeNonce = buf.readInt();
        environmentJvmArgs = readStringList(buf);
        environmentSystemProperties = readStringList(buf);
        environmentPropertyPatterns = readStringList(buf);
        environmentClassLoaders = readStringList(buf);
        environmentThreads = readStringList(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) { // server -> client only
    }

    private static List<String> readStringList(ByteBuf buf) {
        int size = buf.readShort();
        List<String> list = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            list.add(ByteBufUtils.readUTF8String(buf));
        }
        return list;
    }
}