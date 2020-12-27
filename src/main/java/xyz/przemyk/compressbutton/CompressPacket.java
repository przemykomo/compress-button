package xyz.przemyk.compressbutton;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class CompressPacket {

    public CompressPacket() {}

    public CompressPacket(PacketBuffer buffer) {}
    
    public void toBytes(PacketBuffer buffer) {}

    public void handle(Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.getSender();
            if (sender != null) {
                ItemStack itemStack = sender.inventory.getItemStack();
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
                        sender.inventory.placeItemBackInInventory(sender.world, output);
                        itemStack.setCount(itemStack.getCount() % 9);
                        sender.connection.sendPacket(new SSetSlotPacket(-1, -1, itemStack));
                    });
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
