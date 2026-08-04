package net.fsefmgftc.fseticket.block.entity;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TicketInspectionMachineBlockEntity extends BlockEntity {
	private static final String ERROR_NO_TICKET_SCANNED = "no ticket scanned";
	private static final String ERROR_NOT_SERVER_LEVEL = "not server level";
	private static final String ERROR_NO_SCANNER = "no scanner";
	private static final String ERROR_PLAYER_NOT_FOUND = "player not found";
	private static final String ERROR_NO_VALID_TICKET_OR_CARD = "no valid ticket or card";
	private static final String ERROR_NO_IC_CARD = "no IC card";
	private static final String ERROR_NO_CARD = "no card";
	private static final String ERROR_INSUFFICIENT = "insufficient";

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
			return switch (method) {
				case 0 -> {
					if (lastScannedData == null) yield MethodResult.of(null, ERROR_NO_TICKET_SCANNED);
					yield MethodResult.of(isICCard ? buildICInfo() : buildTicketInfo());
				}
				case 1 -> runOnServer(this::destroyItem);
				case 2 -> {
					double amt = args.optDouble(0, 0.0);
					yield runOnServer(() -> deductICCard(amt));
				}
				case 3 -> {
					String station = args.optString(0, "");
					yield runOnServer(() -> updateTicketState(true, false, station));
				}
				case 4 -> runOnServer(() -> updateTicketState(false, true, ""));
				case 5 -> runOnServer(() -> updateTicketState(false, false, ""));
				default -> MethodResult.of();
			};
		}

		private MethodResult runOnServer(Runnable action) {
			if (level instanceof net.minecraft.server.level.ServerLevel sl) {
				sl.getServer().execute(action);
				return MethodResult.of(true);
			}
			return MethodResult.of(false, ERROR_NOT_SERVER_LEVEL);
		}

		private MethodResult destroyItem() {
			Player p = getLastScanner();
			if (p == null) {
				return MethodResult.of(false, level == null || lastScannerUUID == null ? ERROR_NO_SCANNER : ERROR_PLAYER_NOT_FOUND);
			}
			p.setItemInHand(lastScanHand, ItemStack.EMPTY);
			lastScannedData = null;
			setChanged();
			return MethodResult.of(true);
		}

		private MethodResult updateTicketState(boolean entered, boolean exited, String stationId) {
			Player p = getLastScanner();
			if (p == null) {
				return MethodResult.of(false, level == null || lastScannerUUID == null ? ERROR_NO_SCANNER : ERROR_PLAYER_NOT_FOUND);
			}
			ItemStack h = p.getItemInHand(lastScanHand);
			Item item = h.getItem();
			boolean valid = isCurrentHeldItemValid(item);
			if (!valid) {
				return MethodResult.of(false, ERROR_NO_VALID_TICKET_OR_CARD);
			}
			CompoundTag t = h.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			t.putBoolean(TicketDataUtil.ENTERED, entered);
			t.putBoolean(TicketDataUtil.EXITED, exited);
			if (entered && stationId != null && !stationId.isEmpty()) {
				t.putString(TicketDataUtil.ENTRY_STATION, stationId);
			} else if (exited || (!entered && !exited)) {
				t.remove(TicketDataUtil.ENTRY_STATION);
			}
			h.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
			syncHeldItem(p);
			lastScannedData = t;
			setChanged();
			Map<String, Object> info = isICCard ? buildICInfo() : buildTicketInfo();
			peripheral.pushToComputers(isICCard ? "ic_card_state_updated" : "ticket_state_updated", info);
			return MethodResult.of(true, info);
		}

		private MethodResult deductICCard(double amt) {
			if (!isICCard) {
				return MethodResult.of(false, ERROR_NO_IC_CARD);
			}
			Player p = getLastScanner();
			if (p == null) {
				return MethodResult.of(false, level == null || lastScannerUUID == null ? ERROR_NO_IC_CARD : ERROR_PLAYER_NOT_FOUND);
			}
			ItemStack h = p.getItemInHand(lastScanHand);
			if (h.getItem() != FseticketModItems.IC_CARD.get()) {
				return MethodResult.of(false, ERROR_NO_CARD);
			}
			CompoundTag t = h.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			double bal = t.getDouble(TicketDataUtil.BALANCE);
			if (bal < amt) {
				return MethodResult.of(false, ERROR_INSUFFICIENT);
			}
			t.putDouble(TicketDataUtil.BALANCE, bal - amt);
			h.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
			syncHeldItem(p);
			lastScannedData = t;
			setChanged();
			return MethodResult.of(true, t.getDouble(TicketDataUtil.BALANCE));
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
		return TicketDataUtil.buildTicketInfo(lastScannedData, lastScannerName);
	}

	private Map<String, Object> buildICInfo() {
		return TicketDataUtil.buildICCardInfo(lastScannedData, lastScannerName);
	}

	public ItemInteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide()) return ItemInteractionResult.sidedSuccess(true);

		ItemStack held = player.getItemInHand(hand);
		Item hi = held.getItem();
		boolean isCard = hi == FseticketModItems.IC_CARD.get();

		if (isCard || isTicketItem(hi)) {
			captureScan(player, hand, held, isCard);
			setChanged();
			peripheral.pushToComputers(isCard ? "ic_card_scanned" : "ticket_scanned", isCard ? buildICInfo() : buildTicketInfo());
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

	private Player getLastScanner() {
		if (lastScannerUUID == null || level == null) {
			return null;
		}
		return level.getPlayerByUUID(lastScannerUUID);
	}

	private boolean isCurrentHeldItemValid(Item item) {
		return isICCard ? item == FseticketModItems.IC_CARD.get() : isTicketItem(item);
	}

	private boolean isTicketItem(Item item) {
		return item == FseticketModItems.LOCAL_TICKET.get()
			|| item == FseticketModItems.EXP_TICKET.get()
			|| item == FseticketModItems.SINGLETRIP_TICKET.get()
			|| item == FseticketModItems.FSE_PASS.get();
	}

	private void captureScan(Player player, InteractionHand hand, ItemStack heldItem, boolean scannedICCard) {
		lastScannedData = heldItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		lastScannerUUID = player.getUUID();
		lastScannerName = player.getName().getString();
		lastScanHand = hand;
		isICCard = scannedICCard;
	}
}
