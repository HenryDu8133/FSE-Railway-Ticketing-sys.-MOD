package net.fsefmgftc.fseticket.block.entity;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.fsefmgftc.fseticket.block.ICRefillMachineBlock;
import net.fsefmgftc.fseticket.init.FseticketModBlockEntities;
import net.fsefmgftc.fseticket.init.FseticketModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ICRefillMachineBlockEntity extends BlockEntity {
	private final RefillPeripheral peripheral = new RefillPeripheral();
	private ItemStack insertedCard = ItemStack.EMPTY;

	public ICRefillMachineBlockEntity(BlockPos pos, BlockState state) {
		super(FseticketModBlockEntities.IC_REFILL_MACHINE.get(), pos, state);
	}

	public IPeripheral getPeripheral() {
		return peripheral;
	}

	private class RefillPeripheral implements IPeripheral, IDynamicPeripheral {
		private final Set<IComputerAccess> computers = new HashSet<>();

		@Override
		public String getType() {
			return "ic_refill_machine";
		}

		@Override
		public void attach(IComputerAccess c) {
			computers.add(c);
		}

		@Override
		public void detach(IComputerAccess c) {
			computers.remove(c);
		}

		@Override
		public boolean equals(IPeripheral o) {
			return this == o;
		}

		@Override
		public String[] getMethodNames() {
			return new String[] { "getCardInfo", "refill", "deduct" };
		}

		@Override
		public MethodResult callMethod(IComputerAccess comp, ILuaContext ctx, int method, IArguments args) throws LuaException {
			if (insertedCard.isEmpty()) {
				return MethodResult.of(null, "no card");
			}
			CompoundTag t = insertedCard.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			switch (method) {
				case 0:
					Map<String, Object> info = new LinkedHashMap<>();
					info.put("cardId", t.getString("cardId"));
					info.put("ownerName", t.getString("ownerName"));
					info.put("balance", t.getDouble("balance"));
					return MethodResult.of(info);
				case 1:
					t.putDouble("balance", t.getDouble("balance") + args.getDouble(0));
					insertedCard.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
					setChanged();
					return MethodResult.of(true, t.getDouble("balance"));
				case 2:
					double amt = args.getDouble(0);
					double bal = t.getDouble("balance");
					if (bal < amt) {
						return MethodResult.of(false, "insufficient");
					}
					t.putDouble("balance", bal - amt);
					insertedCard.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
					setChanged();
					return MethodResult.of(true, t.getDouble("balance"));
				default:
					return MethodResult.of();
			}
		}
	}

	public ItemInteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide()) {
			return ItemInteractionResult.sidedSuccess(true);
		}
		ItemStack held = player.getItemInHand(hand);
		if (held.isEmpty() && !insertedCard.isEmpty()) {
			if (!player.getInventory().add(insertedCard.copy())) {
				level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, insertedCard.copy()));
			}
			insertedCard = ItemStack.EMPTY;
			level.setBlock(pos, state.setValue(ICRefillMachineBlock.HAS_CARD, false), 3);
			setChanged();
			return ItemInteractionResult.sidedSuccess(false);
		}
		if (held.getItem() == FseticketModItems.IC_CARD.get()) {
			if (!insertedCard.isEmpty()) {
				return ItemInteractionResult.FAIL;
			}
			insertedCard = held.copy();
			insertedCard.setCount(1);
			held.shrink(1);
			level.setBlock(pos, state.setValue(ICRefillMachineBlock.HAS_CARD, true), 3);
			setChanged();
			return ItemInteractionResult.sidedSuccess(false);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
		super.saveAdditional(tag, r);
		if (!insertedCard.isEmpty()) {
			tag.put("Card", insertedCard.save(r));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
		super.loadAdditional(tag, r);
		if (tag.contains("Card")) {
			insertedCard = ItemStack.parse(r, tag.getCompound("Card")).orElse(ItemStack.EMPTY);
		}
	}
}
