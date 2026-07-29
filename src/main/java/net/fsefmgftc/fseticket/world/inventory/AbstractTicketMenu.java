package net.fsefmgftc.fseticket.world.inventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.fsefmgftc.fseticket.init.FseticketModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractTicketMenu extends AbstractContainerMenu implements FseticketModMenus.MenuAccessor {
	public final Map<String, Object> menuState = new HashMap<>();
	public final Level world;
	public final Player entity;
	public int x;
	public int y;
	public int z;
	public String startNameEn = "";
	public String terminalNameEn = "";
	public String startStation = "";
	public String terminalStation = "";
	public String fromNameCnU = "";
	public String toNameCnU = "";
	public String ticketId = "";
	public String orderDatetime = "";
	public int rides = 0;
	public double cost = 0D;

	private final Map<Integer, Slot> customSlots = new HashMap<>();

	protected AbstractTicketMenu(MenuType<?> menuType, int id, Inventory inventory, FriendlyByteBuf buffer) {
		super(menuType, id);
		this.entity = inventory.player;
		this.world = inventory.player.level();
		readTicketData(buffer);
	}

	private void readTicketData(FriendlyByteBuf buffer) {
		if (buffer == null) {
			return;
		}

		BlockPos pos = buffer.readBlockPos();
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
		buffer.readByte();
		startNameEn = buffer.readUtf();
		terminalNameEn = buffer.readUtf();
		startStation = buffer.readUtf();
		terminalStation = buffer.readUtf();
		fromNameCnU = buffer.readUtf();
		toNameCnU = buffer.readUtf();
		rides = buffer.readInt();
		ticketId = buffer.readUtf();
		cost = buffer.readDouble();
		orderDatetime = buffer.readUtf();
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public Map<Integer, Slot> getSlots() {
		return Collections.unmodifiableMap(customSlots);
	}

	@Override
	public Map<String, Object> getMenuState() {
		return menuState;
	}
}
