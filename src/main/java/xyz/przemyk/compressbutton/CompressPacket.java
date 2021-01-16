package xyz.przemyk.compressbutton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.network.NetworkEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CompressPacket {

    private final boolean shift;

    public CompressPacket() {
        this(InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT));
    }

    public CompressPacket(boolean shift) {
        this.shift = shift;
    }

    public CompressPacket(PacketBuffer buffer) {
        this.shift = buffer.readBoolean();
    }
    
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBoolean(shift);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.getSender();
            if (sender != null) {
                if (shift) {
                    Map<Item, ItemStack> itemMap = new HashMap<>();
                    for (int i = 0; i < 36; ++i) {
                        ItemStack itemStack = sender.inventory.getStackInSlot(i);
                        Item item = itemStack.getItem();
                        if (!itemStack.isEmpty() && item.getMaxDamage(itemStack) == 0) {
                            if (itemMap.containsKey(item)) {
                                itemMap.get(item).grow(itemStack.getCount());
                            } else {
                                itemMap.put(item, itemStack.copy());
                            }
                            itemStack.setCount(0);
                        }
//                        if (compressItem(itemStack, sender)) {
                            sender.connection.sendPacket(new SSetSlotPacket(-1, i, itemStack));
//                        }
                    }

                    for (ItemStack itemStack : itemMap.values()) {
                        compressItem(itemStack, sender);
                        if (!itemStack.isEmpty()) {
                            sender.inventory.placeItemBackInInventory(sender.world, itemStack);
                        }
                    }
                    sender.connection.sendPacket(new SSetSlotPacket(-1, -1, sender.inventory.getItemStack()));
                } else {
                    ItemStack itemStack = sender.inventory.getItemStack();
                    if (compressItem(itemStack, sender)) {
                        sender.connection.sendPacket(new SSetSlotPacket(-1, -1, itemStack));
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    private boolean compressItem(ItemStack itemStack, ServerPlayerEntity sender) {
        if (itemStack.getCount() >= 9) {
            Optional<IRecipe<?>> craftingRecipe = sender.world.getRecipeManager().getRecipes().stream().filter(
                    recipe -> {
                        if (recipe.getType() == IRecipeType.CRAFTING) {
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

            craftingRecipe.ifPresent(recipe -> {
                ItemStack recipeOutput = recipe.getRecipeOutput();
                ItemStack output = new ItemStack(recipeOutput.getItem(), recipeOutput.getCount() * itemStack.getCount() / 9);
                itemStack.setCount(itemStack.getCount() % 9);
                sender.inventory.placeItemBackInInventory(sender.world, output);
            });

            return craftingRecipe.isPresent();
        }
        return false;
    }
}
