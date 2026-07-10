
package net.fsefmgftc.fseticket.procedures;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import net.fsefmgftc.fseticket.network.FseticketModVariables;

public final class LoadTicketDataProcedure {
	private LoadTicketDataProcedure() {
	}

	public static void execute(Entity entity) {
		if (entity instanceof Player player) {
			execute(player);
		}
	}

	public static void execute(Player player) {
		if (player == null) {
			return;
		}
		ItemStack stack = player.getMainHandItem();
		if (stack.isEmpty()) {
			stack = player.getOffhandItem();
		}
		execute(player, stack);
	}

	public static void execute(Player player, ItemStack stack) {
		if (player == null || stack == null || stack.isEmpty()) {
			return;
		}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		FseticketModVariables.PlayerVariables vars = player.getData(FseticketModVariables.PLAYER_VARIABLES);
		vars.ticketRides = tag.getInt("rides");
		vars.ticketId = tag.getString("ticketId");
		vars.cost = tag.getDouble("cost");
		vars.order_datetime = tag.getString("order_datetime");
		vars.start_name_en = tag.getString("start_name_en");
		vars.terminal_name_en = tag.getString("terminal_name_en");
		vars.markSyncDirty();
	}
}
