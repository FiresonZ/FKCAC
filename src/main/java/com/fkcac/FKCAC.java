package com.fkcac;

import com.fkcac.hook.SpoofConfig;
import com.fkcac.network.FKCACProtocolHandler;
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
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * FKCAC (Fuck CatAntiCheat) - a Minecraft 1.7.10 Forge client mod that takes over the
 * "CatAntiCheat" plugin channel and answers the CatAntiCheat server-side checks with
 * spoofed, client-controlled responses.
 *
 * <p>Usage note: the original CatAntiCheat client mod must be removed from {@code mods/}
 * before loading this one, otherwise FML will reject the duplicate "CatAntiCheat" channel.
 */
@Mod(modid = FKCAC.MODID, name = FKCAC.NAME, version = Tags.VERSION)
public class FKCAC {
    public static final String MODID = "fkcac";
    public static final String NAME = "FKCAC";

    /** CatAntiCheat client protocol version reported during the handshake. */
    public static final int PROTOCOL_VERSION = 2;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static FKCAC instance;
    public static SimpleNetworkWrapper networkChannel;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;

        SpoofConfig.load(event);

        // Take over the channel that the server-side CatAntiCheat plugin expects.
        networkChannel = NetworkRegistry.INSTANCE.newSimpleChannel("CatAntiCheat");

        // Server -> client requests (received on the client).
        networkChannel.registerMessage(FKCACProtocolHandler.HelloHandler.class, SPacketHello.class, 0, Side.CLIENT);
        networkChannel.registerMessage(FKCACProtocolHandler.FileCheckHandler.class, SPacketFileCheck.class, 1, Side.CLIENT);
        networkChannel.registerMessage(FKCACProtocolHandler.ClassCheckHandler.class, SPacketClassCheck.class, 2, Side.CLIENT);
        networkChannel.registerMessage(FKCACProtocolHandler.ScreenshotHandler.class, SPacketScreenshot.class, 3, Side.CLIENT);
        networkChannel.registerMessage(FKCACProtocolHandler.DataCheckHandler.class, SPacketDataCheck.class, 9, Side.CLIENT);

        // Client -> server packets (registered on the server side so the wrapper knows the types).
        networkChannel.registerMessage(FKCACProtocolHandler.HelloReplyHandler.class, CPacketHelloReply.class, 4, Side.SERVER);
        networkChannel.registerMessage(FKCACProtocolHandler.FileHashHandler.class, CPacketFileHash.class, 5, Side.SERVER);
        networkChannel.registerMessage(FKCACProtocolHandler.ClassFoundHandler.class, CPacketClassFound.class, 6, Side.SERVER);
        networkChannel.registerMessage(FKCACProtocolHandler.InjectDetectHandler.class, CPacketInjectDetect.class, 7, Side.SERVER);
        networkChannel.registerMessage(FKCACProtocolHandler.ImageDataHandler.class, CPacketImageData.class, 8, Side.SERVER);
        networkChannel.registerMessage(FKCACProtocolHandler.VanillaDataHandler.class, CPacketVanillaData.class, 10, Side.SERVER);

        LOGGER.info("FKCAC registered 'CatAntiCheat' channel (protocol {})", PROTOCOL_VERSION);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("FKCAC initialised");
    }
}