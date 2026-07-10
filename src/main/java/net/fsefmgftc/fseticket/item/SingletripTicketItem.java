package net.fsefmgftc.fseticket.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import java.util.List;

public class SingletripTicketItem extends Item {
	public SingletripTicketItem(){super(new Item.Properties().stacksTo(1).fireResistant().component(DataComponents.CUSTOM_DATA,def()));}
	private static CustomData def(){CompoundTag t=new CompoundTag();t.putString("type","single");t.putString("ticketId","");t.putString("start_name_en","");t.putString("terminal_name_en","");t.putString("start_station","");t.putString("terminal_station","");t.putString("fromNameCnU","");t.putString("toNameCnU","");t.putInt("rides",1);t.putBoolean("entered",false);t.putBoolean("exited",false);t.putLong("timestamp",0);t.putDouble("cost",0);t.putString("order_datetime","");return CustomData.of(t);}

	@Override public void appendHoverText(ItemStack s,TooltipContext ctx,List<Component> list,TooltipFlag f){
		super.appendHoverText(s,ctx,list,f);
		CompoundTag t=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
		String from=t.getString("start_name_en");String to=t.getString("terminal_name_en");
		String route=(from.isEmpty()||from.equals("\"\""))?"---":(from+" → "+to);
		list.add(Component.literal("§7区间: §f"+route));
		list.add(Component.literal(t.getBoolean("entered")?"§a已进站":t.getBoolean("exited")?"§c已出站":"§7未进站"));
	}
}
