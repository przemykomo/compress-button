package xyz.przemyk.compressbutton;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(CompressButtonMod.MODID)
public class CompressButtonMod {
    public static final String MODID = "compressbutton";
    public static final ResourceLocation COMPRESS_BUTTON_TEXTURE = new ResourceLocation(MODID, "textures/gui/compress_button.png");
    public static final int X_OFFSET = 130;

    public CompressButtonMod() {
        CompressPacketHandler.init();
        MinecraftForge.EVENT_BUS.addListener(CompressButtonMod::initGui);
    }

    public static void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        if (screen instanceof InventoryScreen) {
            InventoryScreen inventoryScreen = (InventoryScreen) screen;
            event.addWidget(new CompressButton(inventoryScreen.getGuiLeft() + X_OFFSET, inventoryScreen.height / 2 - 22,
                    20, 18, 0, 0, 19, COMPRESS_BUTTON_TEXTURE,
                    b -> CompressPacketHandler.INSTANCE.sendToServer(new CompressPacket()), inventoryScreen));
        }
    }
}
