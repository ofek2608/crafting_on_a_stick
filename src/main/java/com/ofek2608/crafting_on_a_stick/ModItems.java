package com.ofek2608.crafting_on_a_stick;

import com.ofek2608.crafting_on_a_stick.integration.COASCurios;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ModItems {
	private ModItems() {}
	public static void load() {}


	private static final DeferredRegister<Item> REGISTER;
	static {
		REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, CraftingOnAStick.ID);
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}







	private static boolean doPlayerHave(Player player, RegistryObject<ItemOnAStick> itemReg) {
		Item item = itemReg.get();
		
		IItemHandlerModifiable inventory = COASCurios.getFullInventory(player);
		int size = inventory.getSlots();
		
		for (int i = 0; i < size; i++) {
			ItemStack invStack = inventory.getStackInSlot(i);
			if (!invStack.isEmpty() && invStack.getItem() == item)
				return true;
		}

		return false;
	}








	public static final RegistryObject<ItemOnAStick> CRAFTING_TABLE = createItem(Blocks.CRAFTING_TABLE, "crafting",
			(a,b,c)->new CraftingMenu(a,b,c) {
				@Override
				public boolean stillValid(Player player) {
					return doPlayerHave(player, CRAFTING_TABLE);
				}
			});
	public static final RegistryObject<ItemOnAStick> LOOM = createItem(
			Blocks.LOOM,
			"loom",
			(a,b,c)->new LoomMenu(a,b,c) {
				@Override
				public boolean stillValid(Player player) {
					return doPlayerHave(player, LOOM);
				}
			});
	public static final RegistryObject<ItemOnAStick> GRINDSTONE = createItem(
			Blocks.GRINDSTONE,
			"grindstone_title",
			(a,b,c)->new GrindstoneMenu(a,b,c) {
				@Override
				public boolean stillValid(Player player) {
					return doPlayerHave(player, GRINDSTONE);
				}
			});
	public static final RegistryObject<ItemOnAStick> CARTOGRAPHY_TABLE = createItem(
			Blocks.CARTOGRAPHY_TABLE,
			"cartography_table",
			(a,b,c)->new CartographyTableMenu(a,b,c) {
				@Override
				public boolean stillValid(Player player) {
					return doPlayerHave(player, CARTOGRAPHY_TABLE);
				}
			});
	public static final RegistryObject<ItemOnAStick> STONECUTTER = createItem(
			Blocks.STONECUTTER,
			"stonecutter",
			(a,b,c)->new StonecutterMenu(a,b,c) {
				@Override
				public boolean stillValid(Player player) {
					return doPlayerHave(player, STONECUTTER);
				}
			});
	public static final RegistryObject<ItemOnAStick> SMITHING_TABLE = createItem(
			Blocks.SMITHING_TABLE,
			"upgrade",
			(a,b,c)->new SmithingMenu(a,b,c) {
				@Override
				public boolean stillValid(Player player) {
					return doPlayerHave(player, SMITHING_TABLE);
				}
			});
	public static final RegistryObject<ItemOnAStick> ANVIL         = createAnvil(Blocks.ANVIL);
	public static final RegistryObject<ItemOnAStick> CHIPPED_ANVIL = createAnvil(Blocks.CHIPPED_ANVIL);
	public static final RegistryObject<ItemOnAStick> DAMAGED_ANVIL = createAnvil(Blocks.DAMAGED_ANVIL);













	private static RegistryObject<ItemOnAStick> createItem(Block block, String containerName, MinecraftMenuBuilder builder) {
		String path = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getPath();
		return REGISTER.register(path, ()->new ItemOnAStick(path, containerName, builder));
	}

	private static RegistryObject<ItemOnAStick> createAnvil(Block block) {
		return createItem(block, "repair", (a,b,c)->new AnvilMenu(a,b,c) {
			@Override
			public boolean stillValid(Player player) {
				return  doPlayerHave(player, DAMAGED_ANVIL) ||
						doPlayerHave(player, CHIPPED_ANVIL) ||
						doPlayerHave(player, ANVIL);
			}

			@Override
			protected void onTake(Player p_150474_, ItemStack p_150475_) {
				if (!p_150474_.getAbilities().instabuild) {
					p_150474_.giveExperienceLevels(-this.getCost());
				}

				float breakChance = ForgeHooks.onAnvilRepair(p_150474_, p_150475_, this.inputSlots.getItem(0), this.inputSlots.getItem(1));

				this.inputSlots.setItem(0, ItemStack.EMPTY);
				if (this.repairItemCountCost > 0) {
					ItemStack itemstack = this.inputSlots.getItem(1);
					if (!itemstack.isEmpty() && itemstack.getCount() > this.repairItemCountCost) {
						itemstack.shrink(this.repairItemCountCost);
						this.inputSlots.setItem(1, itemstack);
					} else {
						this.inputSlots.setItem(1, ItemStack.EMPTY);
					}
				} else {
					this.inputSlots.setItem(1, ItemStack.EMPTY);
				}

				this.setMaximumCost(0);
				this.access.execute((p_150479_, p_150480_) ->
						p_150479_.levelEvent(damageAnvil(p_150474_, breakChance) ? 1029 : 1030, p_150480_, 0));
			}
		});
	}











	private static boolean damageAnvil(Player player, float breakChance) {
		if (player.getAbilities().instabuild || player.getRandom().nextFloat() >= breakChance)
			return false;

		ItemStack candidate;

		candidate = damageAnvilItemStack(player.getItemInHand(InteractionHand.MAIN_HAND));
		if (candidate != null) {
			player.setItemInHand(InteractionHand.MAIN_HAND, candidate);
			return candidate.isEmpty();
		}

		candidate = damageAnvilItemStack(player.getItemInHand(InteractionHand.OFF_HAND));
		if (candidate != null) {
			player.setItemInHand(InteractionHand.OFF_HAND, candidate);
			return candidate.isEmpty();
		}

//		Inventory inventory = player.getInventory();
//		int invSize = inventory.getContainerSize();
//		for (int i = 0; i < invSize; i++) {
//			candidate = damageAnvilItemStack(inventory.getItem(i));
//			if (candidate != null) {
//				inventory.setItem(i, candidate);
//				return candidate.isEmpty();
//			}
//		}
		
		IItemHandlerModifiable inventory = COASCurios.getFullInventory(player);
		int invSize = inventory.getSlots();
		for (int i = 0; i < invSize; i++) {
			candidate = damageAnvilItemStack(inventory.getStackInSlot(i));
			if (candidate != null) {
				inventory.setStackInSlot(i, candidate);
				return candidate.isEmpty();
			}
		}

		return false;
	}

	@Nullable
	private static ItemStack damageAnvilItemStack(ItemStack stack) {
		if (stack.isEmpty())
			return null;
		Item item = stack.getItem();
		if (item == ANVIL.get())
			return new ItemStack(CHIPPED_ANVIL.get());
		if (item == CHIPPED_ANVIL.get())
			return new ItemStack(DAMAGED_ANVIL.get());
		if (item == DAMAGED_ANVIL.get())
			return ItemStack.EMPTY;
		return null;
	}
}
