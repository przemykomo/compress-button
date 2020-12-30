package xyz.przemyk.compressbutton;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CompressButtonMod.MODID)
public class ClientEvents {
    public static final ResourceLocation COMPRESS_BUTTON_TEXTURE = new ResourceLocation(CompressButtonMod.MODID, "textures/gui/compress_button.png");

    @SubscribeEvent
    public static void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        if (screen instanceof InventoryScreen) {
            InventoryScreen inventoryScreen = (InventoryScreen) screen;
            event.addWidget(new CompressButton(inventoryScreen.getGuiLeft() + CompressButtonMod.X_OFFSET, inventoryScreen.height / 2 - 22,
                    20, 18, 0, 0, 19, COMPRESS_BUTTON_TEXTURE,
                    b -> CompressPacketHandler.INSTANCE.sendToServer(new CompressPacket()), inventoryScreen));
        }
    }
}
