package net.fsefmgftc.fseticket.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.IEventBus;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.fsefmgftc.fseticket.FseticketMod;

public final class FseticketModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FseticketMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FSE_TICKET = REGISTRY.register("fse_ticket",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.fseticket.fse_ticket")).icon(() -> new ItemStack(FseticketModBlocks.TICKET_VENDING_MACHINE.get())).displayItems((parameters, tabData) -> {
				tabData.accept(FseticketModItems.LOCAL_TICKET.get());
				tabData.accept(FseticketModItems.EXP_TICKET.get());
				tabData.accept(FseticketModBlocks.TICKET_VENDING_MACHINE.get().asItem());
				tabData.accept(FseticketModBlocks.TICKET_INSPECTION_MACHINE.get().asItem());
				tabData.accept(FseticketModBlocks.IC_REFILL_MACHINE.get().asItem());
				tabData.accept(FseticketModItems.IC_CARD.get());
				tabData.accept(FseticketModItems.SINGLETRIP_TICKET.get());
				tabData.accept(FseticketModItems.FSE_PASS.get());
			}).build());

	private FseticketModTabs() {
	}

	public static void register(IEventBus eventBus) {
		REGISTRY.register(eventBus);
	}
}
