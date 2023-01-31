package xyz.przemyk.quickcompress;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(CompressButtonMod.MODID)
public class CompressButtonMod {
    public static final String MODID = "quickcompress";

    public CompressButtonMod() {
        CompressPacketHandler.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEvents.addListeners(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }
}
