package net.fsefmgftc.fseticket.block.entity;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.*;

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
import org.jetbrains.annotations.NotNull;

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
        public @NotNull String getType() {
            return "ticket_inspection_machine";
        }

        @Override
        public void attach(@NotNull IComputerAccess computerAccess) {
            computers.add(computerAccess);
        }

        @Override
        public void detach(@NotNull IComputerAccess computerAccess) {
            computers.remove(computerAccess);
        }

        @Override
        public boolean equals(IPeripheral o) {
            return this == o;
        }

        void pushToComputers(String event, Map<String, Object> info) {
            for (IComputerAccess computer : computers) {
                computer.queueEvent(event, info);
            }
        }

        @Override
        public String @NotNull [] getMethodNames() {
            return new String[]{"getLastScanned", "destroyTicket", "deductICCard", "markEntered", "markExited", "resetTicketState"};
        }

        @Override
        public @NotNull MethodResult callMethod(@NotNull IComputerAccess comp, @NotNull ILuaContext ctx, int method, @NotNull IArguments args) throws LuaException {
            switch (method) {
                case 0 -> {
                    if (lastScannedData == null) {
                        return MethodResult.of(null, ERROR_NO_TICKET_SCANNED);
                    }
                    return MethodResult.of(isICCard ? buildICInfo() : buildTicketInfo());
                }

                case 1 -> {
                    return runOnServer(this::destroyItem);
                }

                case 2 -> {
                    double amount = args.getDouble(0);
                    return runOnServer(() -> deductICCard(amount));
                }

                case 3 -> {
                    String stationId = args.count() > 0 ? args.getString(0) : "";
                    return runOnServer(() -> setTicketEntered(stationId));
                }

                case 4 -> {
                    return runOnServer(this::setTicketExited);
                }

                case 5 -> {
                    return runOnServer(() -> setTicketState(false, false, ""));
                }

                default -> {
                    return MethodResult.of();
                }
            }
        }

        private MethodResult runOnServer(Runnable action) {
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.getServer().execute(action);
                return MethodResult.of(true);
            }
            return MethodResult.of(false, ERROR_NOT_SERVER_LEVEL);
        }

        private MethodResult destroyItem() {
            Player lastScanner = getLastScanner();
            if (lastScanner == null) {
                return MethodResult.of(false, level == null || lastScannerUUID == null ? ERROR_NO_SCANNER : ERROR_PLAYER_NOT_FOUND);
            }

            lastScanner.setItemInHand(lastScanHand, ItemStack.EMPTY);
            lastScannedData = null;
            setChanged();
            return MethodResult.of(true);
        }

        private MethodResult setTicketState(boolean entered, boolean exited, String stationId) {
            Player player = getLastScanner();
            if (player == null) {
                return MethodResult.of(false, level == null || lastScannerUUID == null ? ERROR_NO_SCANNER : ERROR_PLAYER_NOT_FOUND);
            }

            ItemStack itemInHand = player.getItemInHand(lastScanHand);
            Item item = itemInHand.getItem();
            if (!isCurrentHeldItemValid(item)) {
                return MethodResult.of(false, ERROR_NO_VALID_TICKET_OR_CARD);
            }

            CompoundTag tag = itemInHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putBoolean(TicketDataUtil.ENTERED, entered);
            tag.putBoolean(TicketDataUtil.EXITED, exited);
            if (entered && !stationId.isEmpty()) {
                tag.putString(TicketDataUtil.ENTRY_STATION, stationId);
            } else if (exited && !entered) {
                tag.remove(TicketDataUtil.ENTRY_STATION);
            }

            itemInHand.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            syncHeldItem(player);
            lastScannedData = tag;
            setChanged();
            peripheral.pushToComputers(isICCard ? "ic_card_state_updated" : "ticket_state_updated", isICCard ? buildICInfo() : buildTicketInfo());
            return MethodResult.of(true, isICCard ? buildICInfo() : buildTicketInfo());
        }

        private MethodResult setTicketEntered(String stationId) {
            return setTicketState(true, false, stationId);
        }

        private MethodResult setTicketExited() {
            return setTicketState(false, true, "");
        }

        private MethodResult deductICCard(double amount) {
            if (!isICCard) {
                return MethodResult.of(false, ERROR_NO_IC_CARD);
            }
            Player lastScanner = getLastScanner();
            if (lastScanner == null) {
                return MethodResult.of(false, level == null || lastScannerUUID == null ? ERROR_NO_IC_CARD : ERROR_PLAYER_NOT_FOUND);
            }
            ItemStack cardItem = lastScanner.getItemInHand(lastScanHand);
            if (cardItem.getItem() != FseticketModItems.IC_CARD.get()) {
                return MethodResult.of(false, ERROR_NO_CARD);
            }
            CompoundTag tag = cardItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            double balance = tag.getDouble(TicketDataUtil.BALANCE);
            if (balance < amount) {
                return MethodResult.of(false, ERROR_INSUFFICIENT);
            }
            tag.putDouble(TicketDataUtil.BALANCE, balance - amount);
            cardItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            syncHeldItem(lastScanner);
            lastScannedData = tag;
            setChanged();
            return MethodResult.of(true, tag.getDouble(TicketDataUtil.BALANCE));
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
        if (level.isClientSide()) {
            return ItemInteractionResult.sidedSuccess(true);
        }
        ItemStack held = player.getItemInHand(hand);
        Item heldItem = held.getItem();
        if (heldItem == FseticketModItems.IC_CARD.get()) {
            captureScan(player, hand, held, true);
            setChanged();
            peripheral.pushToComputers("ic_card_scanned", buildICInfo());
            return ItemInteractionResult.sidedSuccess(false);
        }
        if (isTicketItem(heldItem)) {
            captureScan(player, hand, held, false);
            setChanged();
            peripheral.pushToComputers("ticket_scanned", buildTicketInfo());
            return ItemInteractionResult.sidedSuccess(false);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
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
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
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
