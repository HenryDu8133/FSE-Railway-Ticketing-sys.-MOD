package net.fsefmgftc.fseticket.item;

import java.util.List;
import net.fsefmgftc.fseticket.util.TicketDataUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

public class SingletripTicketItem extends Item {
	public SingletripTicketItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().component(DataComponents.CUSTOM_DATA, def()));
	}

	private static CustomData def() {
		CompoundTag tag = TicketDataUtil.createSingleTripTicketTag();
		return CustomData.of(tag);
	}
	// i18n when
	// todo: add i18n & l10n
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);

		CompoundTag ticketData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		String routeName = ticketData.getString(TicketDataUtil.LINE_NAME);
		if (!TicketDataUtil.hasMeaningfulValue(routeName)) {
			routeName = ticketData.getString(TicketDataUtil.START_NAME_EN);
		}
		if (!TicketDataUtil.hasMeaningfulValue(routeName)) {
			routeName = ticketData.getString(TicketDataUtil.FROM_NAME_CNU);
		}

		tooltip.add(Component.literal("§7区间：§f" + TicketDataUtil.normalizedDisplay(routeName)));
		tooltip.add(Component.literal(
			ticketData.getBoolean(TicketDataUtil.ENTERED)
				? "§a已进站"
				: ticketData.getBoolean(TicketDataUtil.EXITED) ? "§e已出站" : "§7未进站"
		));
	}
}
