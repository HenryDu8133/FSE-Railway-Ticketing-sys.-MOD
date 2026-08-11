package net.fsefmgftc.fseticket.block;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum VendingType implements StringRepresentable {
	NONE("none"),
	LOCAL_TICKET("local_ticket"),
	EXP_TICKET("exp_ticket"),
	IC_CARD("ic_card");

	private final String name;

	VendingType(String name) {
		this.name = name;
	}

	@Override
	public @NotNull String getSerializedName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
