package com.fkcac.network;

import com.fkcac.FKCAC;
import com.fkcac.hook.SpoofConfig;
import com.fkcac.network.message.CPacketClassFound;
import com.fkcac.network.message.CPacketFileHash;
import com.fkcac.network.message.CPacketHelloReply;
import com.fkcac.network.message.CPacketImageData;
import com.fkcac.network.message.CPacketInjectDetect;
import com.fkcac.network.message.CPacketVanillaData;
import com.fkcac.network.message.SPacketClassCheck;
import com.fkcac.network.message.SPacketDataCheck;
import com.fkcac.network.message.SPacketFileCheck;
import com.fkcac.network.message.SPacketHello;
import com.fkcac.network.message.SPacketScreenshot;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Handlers for the "CatAntiCheat" plugin channel. Every server -> client request is
 * answered with a pristine-client response so the (frozen) CatAntiCheat-Public server
 * plugin cannot tell the client has been modified:
 *
 * <ul>
 *   <li>handshake reports the protocol version and echoes the salt;</li>
 *   <li>file check reports allow-listed real hashes (or a pinned list);</li>
 *   <li>class check reports only the server's marker-class candidates as present;</li>
 *   <li>screenshot is answered with a blank PNG;</li>
 *   <li>the periodic data check reports pristine vanilla renderer flags.</li>
 * </ul>
 */
public final class FKCACProtocolHandler {
    /** Salt issued by the server during the handshake; echoed back on later checks. */
    private static byte salt;

    /** 1x1 px valid PNG used as a fake "clean" screenshot. */
    private static final byte[] BLANK_PNG = {
        (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 'I', 'H', 'D', 'R',
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
        (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 'I', 'D', 'A', 'T',
        0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF, (byte) 0xE0, 0x63, 0x60, 0x00,
        0x00, 0x00, 0x07, 0x24, 0x62, (byte) 0x84, (byte) 0xB6,
        0x00, 0x00, 0x00, 0x00, 'I', 'E', 'N', 'D',
        (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    private FKCACProtocolHandler() { }

    /** SPacketHello (0) -> CPacketHelloReply (4): handshake with protocol version + echoed salt. */
    public static final class HelloHandler implements IMessageHandler<SPacketHello, IMessage> {
        @Override
        public IMessage onMessage(SPacketHello message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                salt = message.salt;
            }
            return new CPacketHelloReply(SpoofConfig.protocolVersion(), message.salt);
        }
    }

    /** SPacketFileCheck (1) -> CPacketFileHash (5): allow-listed hash list, computed off-thread. */
    public static final class FileCheckHandler implements IMessageHandler<SPacketFileCheck, IMessage> {
        @Override
        public IMessage onMessage(final SPacketFileCheck message, final MessageContext ctx) {
            if (ctx.side.isClient()) {
                salt = SpoofConfig.refreshSalt(salt);
            }
            final byte checkSalt = salt;
            // Mirror the real client: hashing the launch sources must not block the network thread.
            // SimpleNetworkWrapper.sendToServer is thread-safe, so sending from here is fine.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FKCAC.networkChannel.sendToServer(new CPacketFileHash(SpoofConfig.fileHashList(), checkSalt));
                }
            }).start();
            return null;
        }
    }

    /** SPacketClassCheck (2) -> CPacketClassFound (6): only report the trusted marker classes. */
    public static final class ClassCheckHandler implements IMessageHandler<SPacketClassCheck, IMessage> {
        @Override
        public IMessage onMessage(SPacketClassCheck message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                salt = SpoofConfig.refreshSalt(salt);
            }
            // The server only ever queries black-listed cheat classes plus one marker class it
            // picks from a fixed candidate set; report exactly the trusted candidates as present
            // and hide everything else.
            return new CPacketClassFound(SpoofConfig.filterTrustedClasses(message.getClassList()), salt);
        }
    }

    /** SPacketScreenshot (3) -> CPacketImageData (8) with a blank PNG. */
    public static final class ScreenshotHandler implements IMessageHandler<SPacketScreenshot, IMessage> {
        @Override
        public IMessage onMessage(SPacketScreenshot message, MessageContext ctx) {
            if (SpoofConfig.spoofScreenshot()) {
                return new CPacketImageData(true, BLANK_PNG);
            }
            return null; // send nothing, let the server time out the screenshot check
        }
    }

    /** SPacketDataCheck (9) -> CPacketVanillaData (10): report pristine renderer flags. */
    public static final class DataCheckHandler implements IMessageHandler<SPacketDataCheck, IMessage> {
        @Override
        public IMessage onMessage(SPacketDataCheck message, MessageContext ctx) {
            // A clean client has gamma <= 1.5 (lighting=false) and no transparent texture pack
            // (transparentTexture=false); neither server toggle can flag this.
            return new CPacketVanillaData(false, false);
        }
    }

    // ---- Empty client -> server handlers (registered on Side.SERVER purely so
    // ---- SimpleNetworkWrapper knows the packet types for serialization / sending). ----

    /** CPacketHelloReply (4) */
    public static final class HelloReplyHandler implements IMessageHandler<CPacketHelloReply, IMessage> {
        @Override
        public IMessage onMessage(CPacketHelloReply message, MessageContext ctx) {
            return null;
        }
    }

    /** CPacketFileHash (5) */
    public static final class FileHashHandler implements IMessageHandler<CPacketFileHash, IMessage> {
        @Override
        public IMessage onMessage(CPacketFileHash message, MessageContext ctx) {
            return null;
        }
    }

    /** CPacketClassFound (6) */
    public static final class ClassFoundHandler implements IMessageHandler<CPacketClassFound, IMessage> {
        @Override
        public IMessage onMessage(CPacketClassFound message, MessageContext ctx) {
            return null;
        }
    }

    /** CPacketInjectDetect (7) */
    public static final class InjectDetectHandler implements IMessageHandler<CPacketInjectDetect, IMessage> {
        @Override
        public IMessage onMessage(CPacketInjectDetect message, MessageContext ctx) {
            return null;
        }
    }

    /** CPacketImageData (8) */
    public static final class ImageDataHandler implements IMessageHandler<CPacketImageData, IMessage> {
        @Override
        public IMessage onMessage(CPacketImageData message, MessageContext ctx) {
            return null;
        }
    }

    /** CPacketVanillaData (10) */
    public static final class VanillaDataHandler implements IMessageHandler<CPacketVanillaData, IMessage> {
        @Override
        public IMessage onMessage(CPacketVanillaData message, MessageContext ctx) {
            return null;
        }
    }
}