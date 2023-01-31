package xyz.przemyk.quickcompress;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class CompressPacketHandler {
    private static final String PROTOCOL_VERSION = "3";
    public static SimpleChannel INSTANCE;

    public static void init() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(CompressButtonMod.MODID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        INSTANCE.registerMessage(0,
                CompressPacket.class,
                CompressPacket::toBytes,
                CompressPacket::new,
                CompressPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
