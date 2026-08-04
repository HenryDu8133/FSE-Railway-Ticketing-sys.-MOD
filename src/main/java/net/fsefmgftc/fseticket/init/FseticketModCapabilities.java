package net.fsefmgftc.fseticket.init;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.fsefmgftc.fseticket.FseticketMod;
import net.fsefmgftc.fseticket.block.entity.TicketVendingMachineBlockEntity;
import net.fsefmgftc.fseticket.block.entity.TicketInspectionMachineBlockEntity;
import net.fsefmgftc.fseticket.block.entity.ICRefillMachineBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = FseticketMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class FseticketModCapabilities {
	private FseticketModCapabilities() {
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(PeripheralCapability.get(), FseticketModBlockEntities.TICKET_VENDING_MACHINE.get(),
			(be, side) -> ((TicketVendingMachineBlockEntity) be).getPeripheral());
		event.registerBlockEntity(PeripheralCapability.get(), FseticketModBlockEntities.TICKET_INSPECTION_MACHINE.get(),
			(be, side) -> ((TicketInspectionMachineBlockEntity) be).getPeripheral());
		event.registerBlockEntity(PeripheralCapability.get(), FseticketModBlockEntities.IC_REFILL_MACHINE.get(),
			(be, side) -> ((ICRefillMachineBlockEntity) be).getPeripheral());
	}
}
