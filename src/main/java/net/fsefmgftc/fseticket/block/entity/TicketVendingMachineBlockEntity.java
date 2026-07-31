package net.fsefmgftc.fseticket.block.entity;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.fsefmgftc.fseticket.init.FseticketModItems;
import net.fsefmgftc.fseticket.init.FseticketModBlockEntities;
import net.fsefmgftc.fseticket.util.TicketDataUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TicketVendingMachineBlockEntity extends BlockEntity {
    private final VendingPeripheral peripheral = new VendingPeripheral();

    public TicketVendingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(FseticketModBlockEntities.TICKET_VENDING_MACHINE.get(), pos, state);
    }

    public IPeripheral getPeripheral() {
        return peripheral;
    }

    private class VendingPeripheral implements IPeripheral, IDynamicPeripheral {
        private final Set<IComputerAccess> computers = new HashSet<>();

        @Override
        public @NotNull String getType() {
            return "ticket_vending_machine";
        }

        @Override
        public void attach(@NotNull IComputerAccess computer) {
            computers.add(computer);
        }

        @Override
        public void detach(@NotNull IComputerAccess computer) {
            computers.remove(computer);
        }

        @Override
        public boolean equals(IPeripheral peripheral) {
            return this == peripheral;
        }

        @Override
        public String @NotNull [] getMethodNames() {
            return new String[]{"issueTicket", "issueICCard", "issueFSEPass"};
        }

        @Override
        public @NotNull MethodResult callMethod(@NotNull IComputerAccess comp, @NotNull ILuaContext ctx, int methodIndex, @NotNull IArguments args) throws LuaException {
            return switch (methodIndex) {
                case 0 -> issueTicket(args);
                case 1 -> issueICCard(args);
                case 2 -> issueFSEPass(args);
                default -> MethodResult.of();
            };
        }

        private MethodResult issueTicket(IArguments args) throws LuaException {
            String startName = args.count() > 0 ? args.getString(0) : "???";
            String terminalName = args.count() > 1 ? args.getString(1) : "???";
            String type = args.count() > 2 ? args.getString(2) : TicketDataUtil.TYPE_LOCAL;
            int rides = args.count() > 3 ? Math.max(1, args.getInt(3)) : 1;
            double cost = args.count() > 4 ? args.getDouble(4) : 0D;
            String startStation = args.count() > 5 ? args.getString(5) : "";
            String terminalStation = args.count() > 6 ? args.getString(6) : "";
            String fromNameCn = args.count() > 7 ? args.getString(7) : "";
            String toNameCn = args.count() > 8 ? args.getString(8) : "";

            CompoundTag ticketData = TicketDataUtil.TYPE_SINGLE.equals(type)
                    ? TicketDataUtil.createSingleTripTicketTag()
                    : TicketDataUtil.createBaseTicketTag(type);
            ticketData.putString(TicketDataUtil.START_NAME_EN, startName);
            ticketData.putString(TicketDataUtil.TERMINAL_NAME_EN, terminalName);
            ticketData.putString(TicketDataUtil.LINE_NAME, TicketDataUtil.TYPE_SINGLE.equals(type) ? startName : "");
            ticketData.putString(TicketDataUtil.START_STATION, startStation);
            ticketData.putString(TicketDataUtil.TERMINAL_STATION, terminalStation);
            ticketData.putString(TicketDataUtil.FROM_NAME_CNU, fromNameCn);
            ticketData.putString(TicketDataUtil.TO_NAME_CNU, toNameCn);
            ticketData.putInt(TicketDataUtil.RIDES, rides);

            String ticketId = TicketDataUtil.generateTicketId();
            ticketData.putString(TicketDataUtil.TICKET_ID, ticketId);
            ticketData.putLong(TicketDataUtil.TIMESTAMP, System.currentTimeMillis());
            ticketData.putDouble(TicketDataUtil.COST, cost);
            ticketData.putString(TicketDataUtil.ORDER_DATETIME, TicketDataUtil.currentOrderDateTime());

            ItemStack ticket = new ItemStack(getTicketItem(type));
            ticket.set(DataComponents.CUSTOM_DATA, CustomData.of(ticketData));
            spawnItem(ticket);
            return MethodResult.of(true, ticketId);
        }

        private MethodResult issueICCard(IArguments args) throws LuaException {
            CompoundTag cardData = TicketDataUtil.createICCardTag();
            cardData.putString(TicketDataUtil.CARD_ID, TicketDataUtil.generateCardId());

            String ownerName = args.count() > 0 ? args.getString(0) : "";
            double balance = args.count() > 1 ? args.getDouble(1) : 0D;
            cardData.putString(TicketDataUtil.OWNER_NAME, ownerName);
            cardData.putDouble(TicketDataUtil.BALANCE, balance);
            cardData.putBoolean(TicketDataUtil.ENTERED, false);
            cardData.putString(TicketDataUtil.ENTRY_STATION, "");

            ItemStack card = new ItemStack(FseticketModItems.IC_CARD.get());
            card.set(DataComponents.CUSTOM_DATA, CustomData.of(cardData));
            spawnItem(card);
            return MethodResult.of(true, cardData.getString(TicketDataUtil.CARD_ID));
        }

        private MethodResult issueFSEPass(IArguments args) throws LuaException {
            return MethodResult.of(false);
        }

        private Item getTicketItem(String type) {
            if (TicketDataUtil.TYPE_LIMITED_EXPRESS.equals(type)) {
                return FseticketModItems.EXP_TICKET.get();
            }
            if (TicketDataUtil.TYPE_SINGLE.equals(type)) {
                return FseticketModItems.SINGLETRIP_TICKET.get();
            }
            return FseticketModItems.LOCAL_TICKET.get();
        }

        private void spawnItem(ItemStack item) {
            BlockState state = getBlockState();
            Direction facing = state.hasProperty(HorizontalDirectionalBlock.FACING)
                    ? state.getValue(HorizontalDirectionalBlock.FACING)
                    : Direction.NORTH;
            assert level != null;
            level.addFreshEntity(new ItemEntity(
                    level,
                    worldPosition.getX() + 0.5 + facing.getStepX() * 0.7,
                    worldPosition.getY() + 0.8,
                    worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.7,
                    item
            ));
        }
    }
}
