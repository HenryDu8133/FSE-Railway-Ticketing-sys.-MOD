package net.fsefmgftc.fseticket.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.fsefmgftc.fseticket.FseticketMod;
import net.fsefmgftc.fseticket.block.entity.TicketVendingMachineBlockEntity;
import net.fsefmgftc.fseticket.block.entity.TicketInspectionMachineBlockEntity;
import net.fsefmgftc.fseticket.block.entity.ICRefillMachineBlockEntity;
import java.util.function.Supplier;

public final class FseticketModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FseticketMod.MODID);
	public static final Supplier<BlockEntityType<TicketVendingMachineBlockEntity>> TICKET_VENDING_MACHINE = REGISTRY
		.register("ticket_vending_machine", () -> BlockEntityType.Builder.of(TicketVendingMachineBlockEntity::new, FseticketModBlocks.TICKET_VENDING_MACHINE.get()).build(null));
	public static final Supplier<BlockEntityType<TicketInspectionMachineBlockEntity>> TICKET_INSPECTION_MACHINE = REGISTRY
		.register("ticket_inspection_machine", () -> BlockEntityType.Builder.of(TicketInspectionMachineBlockEntity::new, FseticketModBlocks.TICKET_INSPECTION_MACHINE.get()).build(null));
	public static final Supplier<BlockEntityType<ICRefillMachineBlockEntity>> IC_REFILL_MACHINE = REGISTRY
		.register("ic_refill_machine", () -> BlockEntityType.Builder.of(ICRefillMachineBlockEntity::new, FseticketModBlocks.IC_REFILL_MACHINE.get()).build(null));

	private FseticketModBlockEntities() {
	}

	public static void register(IEventBus eventBus) {
		REGISTRY.register(eventBus);
	}
}
