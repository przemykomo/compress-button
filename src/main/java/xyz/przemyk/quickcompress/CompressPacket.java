package xyz.przemyk.quickcompress;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.network.NetworkEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

public class CompressPacket {

    private final int index;
    private final boolean shift;

    public CompressPacket(FriendlyByteBuf buffer) {
        index = buffer.readVarInt();
        shift = buffer.readBoolean();
    }

    public CompressPacket(int index) {
        this.index = index;
        this.shift = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(index);
        buffer.writeBoolean(shift);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                if (sender.containerMenu != null) {
                    if (shift) {
                        ArrayList<Slot> slotsToCompress = new ArrayList<>();
                        int totalCountToCompress = 0;
                        Slot slot = sender.containerMenu.getSlot(index);
                        ItemStack itemStack = slot.getItem();
                        for (Slot slot1 : sender.containerMenu.slots) {
                            if (slot1.mayPickup(sender) && !(slot1 instanceof ResultSlot) && slot1.isSameInventory(slot) && ItemStack.isSameItemSameTags(itemStack, slot1.getItem())) {
                                slotsToCompress.add(slot1);
                                totalCountToCompress += slot1.getItem().getCount();
                            }
                        }
                        Recipe<?> recipe = compressItem(itemStack, totalCountToCompress, sender).orElse(null);
                        if (recipe != null) {
                            ItemStack recipeOutput = recipe.getResultItem().copy();
                            int outputCount = recipeOutput.getCount() * totalCountToCompress / 9;
                            recipeOutput.setCount(outputCount);
                            int totalCountToTake = totalCountToCompress - (totalCountToCompress % 9);
                            for (Slot slot1 : slotsToCompress) {
                                totalCountToTake -= slot1.safeTake(totalCountToTake, Integer.MAX_VALUE, sender).getCount();
                                if (!slot1.hasItem() && !recipeOutput.isEmpty()) {
                                    slot1.safeInsert(recipeOutput);
                                }
                                if (totalCountToTake == 0) {
                                    if (!recipeOutput.isEmpty()) {
                                        sender.getInventory().placeItemBackInInventory(recipeOutput);
                                    }
                                    break;
                                }
                            }
                        }
                    } else {
                        try {
                            Slot slot = sender.containerMenu.getSlot(index);
                            if (slot.mayPickup(sender) && !(slot instanceof ResultSlot)) {
                                ItemStack itemStack = slot.getItem();
                                compressItem(itemStack, itemStack.getCount(), sender).ifPresent(recipe -> {
                                    ItemStack recipeOutput = recipe.getResultItem();
                                    ItemStack output = new ItemStack(recipeOutput.getItem(), recipeOutput.getCount() * itemStack.getCount() / 9);
                                    slot.safeTake(itemStack.getCount() - (itemStack.getCount() % 9), Integer.MAX_VALUE, sender);
                                    if (sender.containerMenu.getCarried().isEmpty()) {
                                        sender.containerMenu.setCarried(output);
                                    } else {
                                        sender.getInventory().placeItemBackInInventory(output);
                                    }
                                });
                            }
                        } catch (IndexOutOfBoundsException ignored) {}
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    private Optional<Recipe<?>> compressItem(ItemStack itemStack, int count, ServerPlayer sender) {
        if (count >= 9) {
            return sender.level.getRecipeManager().getRecipes().stream().filter(
                    recipe -> {
                        if (recipe.getType() == RecipeType.CRAFTING) {
                            NonNullList<Ingredient> ingredients = recipe.getIngredients();
                            if (ingredients.size() == 9) {
                                for (Ingredient ingredient : ingredients) {
                                    if (!ingredient.test(itemStack)) {
                                        return false;
                                    }
                                }
                                return true;
                            }
                        }
                        return false;
                    }
            ).findFirst();
        }
        return Optional.empty();
    }
}
