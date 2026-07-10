package net.fsefmgftc.fseticket.procedures;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import net.fsefmgftc.fseticket.network.FseticketModVariables;
import net.fsefmgftc.fseticket.network.OpenUrlMessage;
import net.fsefmgftc.fseticket.world.inventory.ExpTicketGUIMenu;
import net.fsefmgftc.fseticket.world.inventory.LocalTicketGUIMenu;

public final class OpenTicketWebProcedure {
	private OpenTicketWebProcedure() {
	}

	public static void execute(Entity entity) {
		if (!(entity instanceof Player player)) {
			return;
		}

		String ticketId = getTicketId(player);
		if (ticketId == null || ticketId.isBlank() || "\"\"".equals(ticketId)) {
			return;
		}

		String url = "https://ticket-detail.fse-media.group/" + ticketId;
		if (player.level().isClientSide) {
			Util.getPlatform().openUri(url);
			return;
		}

		if (player instanceof ServerPlayer serverPlayer) {
			PacketDistributor.sendToPlayer(serverPlayer, new OpenUrlMessage(url));
		}
	}

	private static String getTicketId(Player player) {
		if (player.containerMenu instanceof LocalTicketGUIMenu localMenu) {
			return localMenu.ticketId;
		}
		if (player.containerMenu instanceof ExpTicketGUIMenu expMenu) {
			return expMenu.ticketId;
		}

		ItemStack stack = player.getMainHandItem();
		String id = readTicketIdFromStack(stack);
		if (id != null && !id.isBlank() && !"\"\"".equals(id)) {
			return id;
		}

		stack = player.getOffhandItem();
		id = readTicketIdFromStack(stack);
		if (id != null && !id.isBlank() && !"\"\"".equals(id)) {
			return id;
		}

		return player.getData(FseticketModVariables.PLAYER_VARIABLES).ticketId;
	}

	private static String readTicketIdFromStack(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return null;
		}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		String id = tag.getString("ticketId");
		return id == null || id.isBlank() ? null : id;
	}
}
