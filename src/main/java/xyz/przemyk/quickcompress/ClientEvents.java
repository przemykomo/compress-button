package xyz.przemyk.quickcompress;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    public static KeyMapping compressKey;

    public static void addListeners(IEventBus eventBus) {
        eventBus.addListener(ClientEvents::registerKeyBindings);
    }

    private static void registerKeyBindings(FMLClientSetupEvent event) {
        compressKey = new KeyMapping("key.quickcompress.compress_item", GLFW.GLFW_KEY_C, "key.quickcompress.category");
        event.enqueueWork(() -> ClientRegistry.registerKeyBinding(compressKey));
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (compressKey.getKey().getValue() == event.getKeyCode()) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> containerScreen) {
                double xPos = minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
                double yPos = minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
                Slot slot = containerScreen.findSlot(xPos, yPos);
                if (slot != null && slot.hasItem()) {
                    CompressPacketHandler.INSTANCE.sendToServer(new CompressPacket(slot.index));
                    event.setCanceled(true);
                }
            }
        }
    }
}
