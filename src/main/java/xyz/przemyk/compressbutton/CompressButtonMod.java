package xyz.przemyk.compressbutton;

import net.minecraftforge.fml.common.Mod;

@Mod(CompressButtonMod.MODID)
public class CompressButtonMod {
    public static final String MODID = "compressbutton";
    public static final int X_OFFSET = 130;

    public CompressButtonMod() {
        CompressPacketHandler.init();
    }
}
