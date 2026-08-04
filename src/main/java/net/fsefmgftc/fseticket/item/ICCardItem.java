package net.fsefmgftc.fseticket.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.fsefmgftc.fseticket.util.TicketDataUtil;

public class ICCardItem extends Item {
	public ICCardItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().component(DataComponents.CUSTOM_DATA, def()));
	}

	private static CustomData def() {
		CompoundTag tag = TicketDataUtil.createICCardTag();
		return CustomData.of(tag);
	}

	@Override
	public Component getName(ItemStack s) {
		return Component.literal("FSEICA");
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level w, Player e, InteractionHand h) {
		if (!(e instanceof ServerPlayer sp)) {
			return super.use(w, e, h);
		}

		ItemStack heldItem = e.getItemInHand(h);
		CompoundTag cardData = heldItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		sp.openMenu(new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return Component.literal("IC Card");
			}

			@Override
			public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
				FriendlyByteBuf buffer = TicketDataUtil.createBuffer();
				TicketDataUtil.writeICCardMenuData(buffer, e.blockPosition(), h, cardData);
				return new net.fsefmgftc.fseticket.world.inventory.ICGUIMenu(id, inv, buffer);
			}
		}, buffer -> TicketDataUtil.writeICCardMenuData(buffer, e.blockPosition(), h, cardData));

		return super.use(w, e, h);
	}

	@Override
	public void appendHoverText(ItemStack s, Item.TooltipContext ctx, java.util.List<Component> list, net.minecraft.world.item.TooltipFlag f) {
		super.appendHoverText(s, ctx, list, f);

		CompoundTag cardData = s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		String ownerName = cardData.getString(TicketDataUtil.OWNER_NAME);
		String cardId = cardData.getString(TicketDataUtil.CARD_ID);
		double balance = cardData.getDouble(TicketDataUtil.BALANCE);

		if (TicketDataUtil.hasMeaningfulValue(cardId)) {
			list.add(Component.translatable("tooltip.fseticket.card_id", cardId));
		}

		if (TicketDataUtil.hasMeaningfulValue(ownerName)) {
			list.add(Component.translatable("tooltip.fseticket.owner_name", ownerName));
		}

		list.add(Component.translatable("tooltip.fseticket.balance", String.format("%.2f", balance)));
	}
}
