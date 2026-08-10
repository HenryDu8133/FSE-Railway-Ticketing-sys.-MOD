package net.fsefmgftc.fseticket.block;

import net.minecraft.util.StringRepresentable;

public enum VendingType implements StringRepresentable {
	NONE("none"),
	LOCAL_TICKET("local_ticket"),
	EXP_TICKET("exp_ticket"),
	IC_CARD("ic_card");

	private final String name;

	private VendingType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
