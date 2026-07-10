package net.fsefmgftc.fseticket.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import io.netty.buffer.Unpooled;

public class ICCardItem extends Item {
	public ICCardItem(){super(new Item.Properties().stacksTo(1).fireResistant().component(DataComponents.CUSTOM_DATA,def()));}
	private static CustomData def(){CompoundTag t=new CompoundTag();t.putString("cardId","");t.putString("ownerName","");t.putDouble("balance",0);return CustomData.of(t);}

	@Override public Component getName(ItemStack s){
		String n=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("ownerName");
		if(n!=null&&!n.isEmpty()&&!n.equals("\"\""))return Component.literal("IC Card - "+n);
		return super.getName(s);
	}

	@Override public InteractionResultHolder<ItemStack> use(Level w,Player e,InteractionHand h){
		if(!(e instanceof ServerPlayer sp))return super.use(w,e,h);
		ItemStack s=e.getItemInHand(h);CompoundTag t=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
		sp.openMenu(new MenuProvider(){@Override public Component getDisplayName(){return Component.literal("IC Card");}
		@Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){FriendlyByteBuf b=new FriendlyByteBuf(Unpooled.buffer());b.writeBlockPos(e.blockPosition());b.writeByte(h==InteractionHand.MAIN_HAND?0:1);b.writeUtf(t.getString("cardId"));b.writeUtf(t.getString("ownerName"));b.writeDouble(t.getDouble("balance"));return new net.fsefmgftc.fseticket.world.inventory.ICGUIMenu(id,inv,b);}},buf->{buf.writeBlockPos(e.blockPosition());buf.writeByte(h==InteractionHand.MAIN_HAND?0:1);buf.writeUtf(t.getString("cardId"));buf.writeUtf(t.getString("ownerName"));buf.writeDouble(t.getDouble("balance"));});
		return super.use(w,e,h);
	}

	@Override public void appendHoverText(ItemStack s,Item.TooltipContext ctx,java.util.List<Component> list,net.minecraft.world.item.TooltipFlag f){
		super.appendHoverText(s,ctx,list,f);
		CompoundTag t=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
		String n=t.getString("ownerName");String cid=t.getString("cardId");double bal=t.getDouble("balance");
		if(cid!=null&&!cid.isEmpty()&&!cid.equals("\"\""))list.add(Component.literal("§7卡号: §f"+cid));
		if(n!=null&&!n.isEmpty()&&!n.equals("\"\""))list.add(Component.literal("§7持卡人: §f"+n));
		list.add(Component.literal("§7余额: §6"+String.format("%.2f",bal)));
	}
}