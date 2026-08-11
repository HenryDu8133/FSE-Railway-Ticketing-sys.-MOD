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
import org.jetbrains.annotations.NotNull;

public class ICCardItem extends Item {
	public ICCardItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().component(DataComponents.CUSTOM_DATA, def()));
	}

	private static CustomData def() {
		CompoundTag tag = TicketDataUtil.createICCardTag();
		return CustomData.of(tag);
	}

	@Override
	public @NotNull Component getName(@NotNull ItemStack s) {
		return Component.literal("FSEICA");
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		if (!(player instanceof ServerPlayer sp)) {
			return super.use(level, player, hand);
		}

		ItemStack heldItem = player.getItemInHand(hand);
		CompoundTag cardData = heldItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		sp.openMenu(new MenuProvider() {
			@Override
			public @NotNull Component getDisplayName() {
				return Component.literal("IC Card");
			}

			@Override
			public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
				FriendlyByteBuf buffer = TicketDataUtil.createBuffer();
				TicketDataUtil.writeICCardMenuData(buffer, player.blockPosition(), hand, cardData);
				return new net.fsefmgftc.fseticket.world.inventory.ICGUIMenu(id, inv, buffer);
			}
		}, buffer -> TicketDataUtil.writeICCardMenuData(buffer, player.blockPosition(), hand, cardData));

		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack s, Item.@NotNull TooltipContext ctx, java.util.@NotNull List<Component> list, net.minecraft.world.item.@NotNull TooltipFlag f) {
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
