package net.fsefmgftc.fseticket.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.fsefmgftc.fseticket.item.inventory.LocalTicketInventoryCapability;
import net.fsefmgftc.fseticket.item.inventory.ExpTicketInventoryCapability;
import net.fsefmgftc.fseticket.item.SingletripTicketItem;
import net.fsefmgftc.fseticket.item.LocalTicketItem;
import net.fsefmgftc.fseticket.item.ICCardItem;
import net.fsefmgftc.fseticket.item.FSEPassItem;
import net.fsefmgftc.fseticket.item.ExpTicketItem;
import net.fsefmgftc.fseticket.FseticketMod;

@EventBusSubscriber(modid = FseticketMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class FseticketModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(FseticketMod.MODID);
	public static final DeferredItem<Item> LOCAL_TICKET;
	public static final DeferredItem<Item> EXP_TICKET;
	public static final DeferredItem<Item> TICKET_VENDING_MACHINE;
	public static final DeferredItem<Item> TICKET_INSPECTION_MACHINE;
	public static final DeferredItem<Item> IC_REFILL_MACHINE;
	public static final DeferredItem<Item> IC_CARD;
	public static final DeferredItem<Item> SINGLETRIP_TICKET;
	public static final DeferredItem<Item> FSE_PASS;
	static {
		LOCAL_TICKET = REGISTRY.register("local_ticket", LocalTicketItem::new);
		EXP_TICKET = REGISTRY.register("exp_ticket", ExpTicketItem::new);
		TICKET_VENDING_MACHINE = block(FseticketModBlocks.TICKET_VENDING_MACHINE, new Item.Properties().fireResistant());
		TICKET_INSPECTION_MACHINE = block(FseticketModBlocks.TICKET_INSPECTION_MACHINE, new Item.Properties().fireResistant());
		IC_REFILL_MACHINE = block(FseticketModBlocks.IC_REFILL_MACHINE, new Item.Properties().fireResistant());
		IC_CARD = REGISTRY.register("ic_card", ICCardItem::new);
		SINGLETRIP_TICKET = REGISTRY.register("singletrip_ticket", SingletripTicketItem::new);
		FSE_PASS = REGISTRY.register("fse_pass", FSEPassItem::new);
	}

	private FseticketModItems() {
	}

	public static void register(IEventBus eventBus) {
		REGISTRY.register(eventBus);
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(Capabilities.ItemHandler.ITEM, (stack, context) -> new LocalTicketInventoryCapability(stack), LOCAL_TICKET.get());
		event.registerItem(Capabilities.ItemHandler.ITEM, (stack, context) -> new ExpTicketInventoryCapability(stack), EXP_TICKET.get());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}
