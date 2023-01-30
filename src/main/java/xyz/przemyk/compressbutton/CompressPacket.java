package xyz.przemyk.compressbutton;

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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class CompressPacket {

    private final int index;

//    public CompressPacket() {
//        this(InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT));
//    }

    public CompressPacket(FriendlyByteBuf buffer) {
        index = buffer.readVarInt();
    }

    public CompressPacket(int index) {
        this.index = index;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(index);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
//                if (shift) {
//                    Map<Item, ItemStack> itemMap = new HashMap<>();
//                    for (int i = 0; i < 36; ++i) {
//                        ItemStack itemStack = sender.inventory.getStackInSlot(i);
//                        Item item = itemStack.getItem();
//                        if (!itemStack.isEmpty() && item.getMaxDamage(itemStack) == 0) {
//                            if (itemMap.containsKey(item)) {
//                                itemMap.get(item).grow(itemStack.getCount());
//                            } else {
//                                itemMap.put(item, itemStack.copy());
//                            }
//                            itemStack.setCount(0);
//                        }
////                        if (compressItem(itemStack, sender)) {
//                            sender.connection.sendPacket(new SSetSlotPacket(-1, i, itemStack));
////                        }
//                    }
//
//                    for (ItemStack itemStack : itemMap.values()) {
//                        compressItem(itemStack, sender);
//                        if (!itemStack.isEmpty()) {
//                            sender.inventory.placeItemBackInInventory(sender.world, itemStack);
//                        }
//                    }
//                    sender.connection.sendPacket(new SSetSlotPacket(-1, -1, sender.inventory.getItemStack()));
//                } else {
                if (sender.containerMenu != null) {
                    try {
                        Slot slot = sender.containerMenu.getSlot(index);
                        if (slot.mayPickup(sender) && !(slot instanceof ResultSlot)) {
                            ItemStack itemStack = slot.getItem();
                            if (compressItem(itemStack, sender, slot)) {
//                            sender.connection.sendPacket(new SSetSlotPacket(-1, -1, itemStack));
                            }
                        }
                    } catch (IndexOutOfBoundsException ignored) {}
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    private boolean compressItem(ItemStack itemStack, ServerPlayer sender, Slot slot) {
        if (itemStack.getCount() >= 9) {
            Optional<Recipe<?>> craftingRecipe = sender.level.getRecipeManager().getRecipes().stream().filter(
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

            AtomicBoolean done = new AtomicBoolean(false);

            craftingRecipe.ifPresent(recipe -> {
                ItemStack recipeOutput = recipe.getResultItem();
                ItemStack output = new ItemStack(recipeOutput.getItem(), recipeOutput.getCount() * itemStack.getCount() / 9);
                slot.tryRemove(itemStack.getCount() - (itemStack.getCount() % 9), Integer.MAX_VALUE, sender).ifPresent(takenStack -> {
                    if (sender.containerMenu.getCarried().isEmpty()) {
                        sender.containerMenu.setCarried(output);
                    } else {
                        sender.getInventory().placeItemBackInInventory(output);
                    }
                    done.set(true);
                });
            });

            return done.get();
        }
        return false;
    }
}
