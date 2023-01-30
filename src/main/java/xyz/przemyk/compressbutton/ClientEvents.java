package xyz.przemyk.compressbutton;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    public static KeyMapping compressKey;

    public static void addListeners(IEventBus eventBus) {
        eventBus.addListener(ClientEvents::registerKeyBindings);
    }

    private static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        compressKey = new KeyMapping("key.compress_item", GLFW.GLFW_KEY_C, "key.compressbutton.category");
        event.register(compressKey);
    }

    @SubscribeEvent
    public static void onClientPlayerTick(TickEvent.PlayerTickEvent event) {

    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (compressKey.getKey().getValue() == event.getKeyCode()) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> containerScreen) {
                double xPos = minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
                double yPos = minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
                Slot slot = containerScreen.findSlot(xPos, yPos);
                if (slot != null && slot.getItem().getCount() >= 9) {
                    CompressPacketHandler.INSTANCE.sendToServer(new CompressPacket(slot.index));
                    event.setCanceled(true);
                }
            }
        }
    }

//    @SubscribeEvent
//    public static void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
//        Screen screen = event.getGui();
//        if (screen instanceof InventoryScreen) {
//            InventoryScreen inventoryScreen = (InventoryScreen) screen;
//            event.addWidget(new CompressButton(inventoryScreen.getGuiLeft() + CompressButtonMod.X_OFFSET, inventoryScreen.height / 2 - 22,
//                    20, 18, 0, 0, 19, COMPRESS_BUTTON_TEXTURE,
//                    b -> CompressPacketHandler.INSTANCE.sendToServer(new CompressPacket()), inventoryScreen));
//        }
//    }
}
