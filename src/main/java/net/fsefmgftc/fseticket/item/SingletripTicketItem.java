
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

		tooltip.add(Component.translatable("tooltip.fseticket.ticket_range", TicketDataUtil.normalizedDisplay(routeName)));
		tooltip.add(Component.translatable(
			ticketData.getBoolean(TicketDataUtil.ENTERED)
				? "tooltip.fseticket.entered"
				: ticketData.getBoolean(TicketDataUtil.EXITED)
				  ? "tooltip.fseticket.exited"
				  : "tooltip.fseticket.not_entered"
		));
	}
}
