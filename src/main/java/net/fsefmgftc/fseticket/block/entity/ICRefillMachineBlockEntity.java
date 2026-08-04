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
import net.fsefmgftc.fseticket.util.TicketDataUtil;
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
import org.jetbrains.annotations.NotNull;

public class ICRefillMachineBlockEntity extends BlockEntity {
	private static final String ERROR_NO_CARD = "no card";
	private static final String ERROR_INSUFFICIENT = "insufficient";

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

		// todo: merge $refill & $deduct into a method
		@Override
		public String @NotNull [] getMethodNames() {
			return new String[] { "getCardInfo", "refill", "deduct", "setBalance" };
		}


		@Override
		public @NotNull MethodResult callMethod(@NotNull IComputerAccess comp, @NotNull ILuaContext ctx, int method, @NotNull IArguments args) throws LuaException {
			if (insertedCard.isEmpty()) {
				return MethodResult.of(null, ERROR_NO_CARD);
			}
			CompoundTag cardData = insertedCard.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			return switch (method) {
				case 0 -> MethodResult.of(buildCardInfo(cardData));
				case 1 -> modifyBalance(cardData, args.optDouble(0, 0.0), false);
				case 2 -> modifyBalance(cardData, -args.optDouble(0, 0.0), true);
				case 3 -> setBalance(cardData, args.optDouble(0, 0.0));
				default -> MethodResult.of();
			};
		}

		private Map<String, Object> buildCardInfo(CompoundTag cardData) {
			Map<String, Object> info = new LinkedHashMap<>();
			info.put(TicketDataUtil.CARD_ID, cardData.getString(TicketDataUtil.CARD_ID));
			info.put(TicketDataUtil.OWNER_NAME, cardData.getString(TicketDataUtil.OWNER_NAME));
			info.put(TicketDataUtil.BALANCE, cardData.getDouble(TicketDataUtil.BALANCE));
			info.put(TicketDataUtil.ENTERED, cardData.getBoolean(TicketDataUtil.ENTERED));
			info.put(TicketDataUtil.ENTRY_STATION, cardData.getString(TicketDataUtil.ENTRY_STATION));
			return info;
		}

		private MethodResult setBalance(CompoundTag cardData, double newBalance) {
			cardData.putDouble(TicketDataUtil.BALANCE, newBalance);
			insertedCard.set(DataComponents.CUSTOM_DATA, CustomData.of(cardData));
			setChanged();
			return MethodResult.of(true, cardData.getDouble(TicketDataUtil.BALANCE));
		}

		private MethodResult modifyBalance(CompoundTag cardData, double delta, boolean blockNegativeResult) {
			double newBalance = cardData.getDouble(TicketDataUtil.BALANCE) + delta;
			if (blockNegativeResult && newBalance < 0) return MethodResult.of(false, ERROR_INSUFFICIENT);

			cardData.putDouble(TicketDataUtil.BALANCE, newBalance);
			insertedCard.set(DataComponents.CUSTOM_DATA, CustomData.of(cardData));
			setChanged();

			return MethodResult.of(true, cardData.getDouble(TicketDataUtil.BALANCE));
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
