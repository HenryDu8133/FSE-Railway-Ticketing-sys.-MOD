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
import java.util.UUID;
import net.fsefmgftc.fseticket.init.FseticketModBlockEntities;
import net.fsefmgftc.fseticket.init.FseticketModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TicketInspectionMachineBlockEntity extends BlockEntity {
	private final InspectionPeripheral peripheral = new InspectionPeripheral();
	private CompoundTag lastScannedData = null;
	private UUID lastScannerUUID = null;
	private String lastScannerName = "";
	private InteractionHand lastScanHand = InteractionHand.MAIN_HAND;
	private boolean isICCard = false;

	public TicketInspectionMachineBlockEntity(BlockPos pos, BlockState state) {
		super(FseticketModBlockEntities.TICKET_INSPECTION_MACHINE.get(), pos, state);
	}

	public IPeripheral getPeripheral() {
		return peripheral;
	}

	private class InspectionPeripheral implements IPeripheral, IDynamicPeripheral {
		private final Set<IComputerAccess> computers = new HashSet<>();

		@Override
		public String getType() {
			return "ticket_inspection_machine";
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

		void pushToComputers(String event, Map<String, Object> info) {
			for (IComputerAccess c : computers) {
				c.queueEvent(event, new Object[] { info });
			}
		}

		@Override
		public String[] getMethodNames() {
			return new String[] { "getLastScanned", "destroyTicket", "deductICCard", "markEntered", "markExited", "resetTicketState" };
		}

		@Override
		public MethodResult callMethod(IComputerAccess comp, ILuaContext ctx, int method, IArguments args) throws LuaException {
			if (method == 0) {
				if (lastScannedData == null) {
					return MethodResult.of(null, "no ticket scanned");
				}
				return MethodResult.of(isICCard ? buildICInfo() : buildTicketInfo());
			}
			if (method == 1) {
				if (level instanceof net.minecraft.server.level.ServerLevel sl) {
					sl.getServer().execute(() -> destroyItem());
					return MethodResult.of(true);
				}
				return MethodResult.of(false, "not server level");
			}
			if (method == 2) {
				double amt = args.getDouble(0);
				if (level instanceof net.minecraft.server.level.ServerLevel sl) {
					sl.getServer().execute(() -> deductICCard(amt));
					return MethodResult.of(true);
				}
				return MethodResult.of(false, "not server level");
			}
			if (method == 3) {
				String stationId = args.count() > 0 ? args.getString(0) : "";
				if (level instanceof net.minecraft.server.level.ServerLevel sl) {
					sl.getServer().execute(() -> updateTicketState(true, false, stationId));
					return MethodResult.of(true);
				}
				return MethodResult.of(false, "not server level");
			}
			if (method == 4) {
				if (level instanceof net.minecraft.server.level.ServerLevel sl) {
					sl.getServer().execute(() -> updateTicketState(false, true, ""));
					return MethodResult.of(true);
				}
				return MethodResult.of(false, "not server level");
			}
			if (method == 5) {
				if (level instanceof net.minecraft.server.level.ServerLevel sl) {
					sl.getServer().execute(() -> updateTicketState(false, false, ""));
					return MethodResult.of(true);
				}
				return MethodResult.of(false, "not server level");
			}
			return MethodResult.of();
		}

		private MethodResult destroyItem() {
			if (lastScannerUUID == null || level == null) {
				return MethodResult.of(false, "no scanner");
			}
			Player p = level.getPlayerByUUID(lastScannerUUID);
			if (p == null) {
				return MethodResult.of(false, "player not found");
			}
			p.setItemInHand(lastScanHand, ItemStack.EMPTY);
			lastScannedData = null;
			setChanged();
			return MethodResult.of(true);
		}

		private MethodResult updateTicketState(boolean entered, boolean exited, String stationId) {
			if (lastScannerUUID == null || level == null) {
				return MethodResult.of(false, "no scanner");
			}
			Player p = level.getPlayerByUUID(lastScannerUUID);
			if (p == null) {
				return MethodResult.of(false, "player not found");
			}
			ItemStack h = p.getItemInHand(lastScanHand);
			Item item = h.getItem();
			boolean valid = isICCard ? (item == FseticketModItems.IC_CARD.get()) : (item == FseticketModItems.LOCAL_TICKET.get() || item == FseticketModItems.EXP_TICKET.get() || item == FseticketModItems.SINGLETRIP_TICKET.get() || item == FseticketModItems.FSE_PASS.get());
			if (!valid) {
				return MethodResult.of(false, "no valid ticket or card");
			}
			CompoundTag t = h.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			t.putBoolean("entered", entered);
			t.putBoolean("exited", exited);
			if (entered && stationId != null && !stationId.isEmpty()) {
				t.putString("entry_station", stationId);
			} else if (exited || (!entered && !exited)) {
				t.remove("entry_station");
			}
			h.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
			syncHeldItem(p);
			lastScannedData = t;
			setChanged();
			peripheral.pushToComputers(isICCard ? "ic_card_state_updated" : "ticket_state_updated", isICCard ? buildICInfo() : buildTicketInfo());
			return MethodResult.of(true, isICCard ? buildICInfo() : buildTicketInfo());
		}

		private MethodResult deductICCard(double amt) {
			if (!isICCard || lastScannerUUID == null || level == null) {
				return MethodResult.of(false, "no IC card");
			}
			Player p = level.getPlayerByUUID(lastScannerUUID);
			if (p == null) {
				return MethodResult.of(false, "player not found");
			}
			ItemStack h = p.getItemInHand(lastScanHand);
			if (h.getItem() != FseticketModItems.IC_CARD.get()) {
				return MethodResult.of(false, "no card");
			}
			CompoundTag t = h.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			double bal = t.getDouble("balance");
			if (bal < amt) {
				return MethodResult.of(false, "insufficient");
			}
			t.putDouble("balance", bal - amt);
			h.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
			syncHeldItem(p);
			lastScannedData = t;
			setChanged();
			return MethodResult.of(true, t.getDouble("balance"));
		}
	}

	private void syncHeldItem(Player player) {
		player.getInventory().setChanged();
		if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
			sp.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket(
				player.containerMenu.containerId,
				player.containerMenu.getStateId(),
				player.containerMenu.getItems(),
				player.containerMenu.getCarried()
			));
		}
	}

	private Map<String, Object> buildTicketInfo() {
		Map<String, Object> info = new LinkedHashMap<>();
		info.put("start_name_en", lastScannedData.getString("start_name_en"));
		info.put("terminal_name_en", lastScannedData.getString("terminal_name_en"));
		info.put("start_station", lastScannedData.getString("start_station"));
		info.put("terminal_station", lastScannedData.getString("terminal_station"));
		info.put("fromNameCnU", lastScannedData.getString("fromNameCnU"));
		info.put("toNameCnU", lastScannedData.getString("toNameCnU"));
		info.put("type", lastScannedData.getString("type"));
		info.put("rides", lastScannedData.getInt("rides"));
		info.put("entered", lastScannedData.getBoolean("entered"));
		info.put("exited", lastScannedData.getBoolean("exited"));
		info.put("ticketId", lastScannedData.getString("ticketId"));
		info.put("timestamp", lastScannedData.getLong("timestamp"));
		info.put("cost", lastScannedData.getDouble("cost"));
		info.put("order_datetime", lastScannedData.getString("order_datetime"));
		info.put("passenger", lastScannerName);
		return info;
	}

	private Map<String, Object> buildICInfo() {
		Map<String, Object> info = new LinkedHashMap<>();
		info.put("cardId", lastScannedData.getString("cardId"));
		info.put("ownerName", lastScannedData.getString("ownerName"));
		info.put("balance", lastScannedData.getDouble("balance"));
		info.put("entered", lastScannedData.getBoolean("entered"));
		info.put("entry_station", lastScannedData.getString("entry_station"));
		info.put("passenger", lastScannerName);
		return info;
	}

	public ItemInteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide()) {
			return ItemInteractionResult.sidedSuccess(true);
		}
		ItemStack held = player.getItemInHand(hand);
		Item hi = held.getItem();
		if (hi == FseticketModItems.IC_CARD.get()) {
			lastScannedData = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			lastScannerUUID = player.getUUID();
			lastScannerName = player.getName().getString();
			lastScanHand = hand;
			isICCard = true;
			setChanged();
			peripheral.pushToComputers("ic_card_scanned", buildICInfo());
			return ItemInteractionResult.sidedSuccess(false);
		}
		if (hi == FseticketModItems.LOCAL_TICKET.get() || hi == FseticketModItems.EXP_TICKET.get() || hi == FseticketModItems.SINGLETRIP_TICKET.get() || hi == FseticketModItems.FSE_PASS.get()) {
			lastScannedData = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			lastScannerUUID = player.getUUID();
			lastScannerName = player.getName().getString();
			lastScanHand = hand;
			isICCard = false;
			setChanged();
			peripheral.pushToComputers("ticket_scanned", buildTicketInfo());
			return ItemInteractionResult.sidedSuccess(false);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
		super.saveAdditional(tag, r);
		if (lastScannedData != null) {
			tag.put("LastScanned", lastScannedData);
		}
		if (lastScannerUUID != null) {
			tag.putUUID("LastScanner", lastScannerUUID);
			tag.putString("LastScannerName", lastScannerName);
			tag.putString("LastScanHand", lastScanHand.name());
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
		super.loadAdditional(tag, r);
		if (tag.contains("LastScanned")) {
			lastScannedData = tag.getCompound("LastScanned");
		}
		if (tag.contains("LastScanner")) {
			lastScannerUUID = tag.getUUID("LastScanner");
			lastScannerName = tag.getString("LastScannerName");
			lastScanHand = InteractionHand.valueOf(tag.getString("LastScanHand"));
		}
	}
}
