package net.fsefmgftc.fseticket.world.inventory;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import net.fsefmgftc.fseticket.init.FseticketModMenus;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class FSEPassGUIMenu extends AbstractContainerMenu implements FseticketModMenus.MenuAccessor {
	public final Map<String, Object> menuState = new HashMap<>() {
		@Override
		public Object put(String key, Object value) {
			if (!this.containsKey(key) && this.size() >= 4)
				return null;
			return super.put(key, value);
		}
	};
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access;
	private final Map<Integer, Slot> customSlots = new HashMap<>();
	private final boolean bound = false;
	private final Supplier<Boolean> boundItemMatcher = null;
	private final Entity boundEntity = null;
	private final BlockEntity boundBlockEntity = null;

	public FSEPassGUIMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(FseticketModMenus.FSE_PASS_GUI.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();
//		this.internal = new ItemStackHandler(0);
		BlockPos pos;
		if (extraData != null) {
			pos = extraData.readBlockPos();
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			this.access = ContainerLevelAccess.create(world, pos);
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		/// Vibed code shouldn't appear this lol.
		/// Since field `bound` is always `false`;
		/// `boundItemMatcher`, `boundBlockEntity` and `boundEntity` are always `null`,
		/// consider `return true` only.
//		if (this.bound) {
//			if (this.boundItemMatcher != null)
//				return this.boundItemMatcher.get();
//			else if (this.boundBlockEntity != null)
//				return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
//			else if (this.boundEntity != null)
//				return this.boundEntity.isAlive();
//		}
        return true;
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
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