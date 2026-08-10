package net.fsefmgftc.fseticket.block;

import net.minecraft.util.StringRepresentable;

public enum InspectionResult implements StringRepresentable {
	NONE("none"),
	SUCCESS("success"),
	FAIL("fail");

	private final String name;

	private InspectionResult(String name) {
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
