package net.fsefmgftc.fseticket.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.world.level.block.Block;

import net.fsefmgftc.fseticket.block.TicketVendingMachineBlock;
import net.fsefmgftc.fseticket.block.TicketInspectionMachineBlock;
import net.fsefmgftc.fseticket.block.ICRefillMachineBlock;
import net.fsefmgftc.fseticket.FseticketMod;

public final class FseticketModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(FseticketMod.MODID);
	public static final DeferredBlock<Block> TICKET_VENDING_MACHINE;
	public static final DeferredBlock<Block> TICKET_INSPECTION_MACHINE;
	public static final DeferredBlock<Block> IC_REFILL_MACHINE;
	static {
		TICKET_VENDING_MACHINE = REGISTRY.register("ticket_vending_machine", TicketVendingMachineBlock::new);
		TICKET_INSPECTION_MACHINE = REGISTRY.register("ticket_inspection_machine", TicketInspectionMachineBlock::new);
		IC_REFILL_MACHINE = REGISTRY.register("ic_refill_machine", ICRefillMachineBlock::new);
	}

	private FseticketModBlocks() {
	}

	public static void register(IEventBus eventBus) {
		REGISTRY.register(eventBus);
	}
}
