package xyz.przemyk.compressbutton;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;

public class CompressButton extends ImageButton {

    private final InventoryScreen inventoryScreen;

    public CompressButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, IPressable onPressIn, InventoryScreen inventoryScreen) {
        super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, onPressIn);
        this.inventoryScreen = inventoryScreen;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        x = inventoryScreen.getGuiLeft() + CompressButtonMod.X_OFFSET;
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }
}
