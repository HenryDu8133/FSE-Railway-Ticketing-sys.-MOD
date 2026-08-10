package net.fsefmgftc.fseticket.block;

import net.minecraft.util.StringRepresentable;

public enum InspectionResult implements StringRepresentable {
	NONE("none"),
	SUCCESS("success"),
	FAIL("fail");

	private final String serializedName;

	InspectionResult(String serializedName) {
		this.serializedName = serializedName;
	}

	@Override
	public String getSerializedName() {
		return this.serializedName;
	}

	@Override
	public String toString() {
		return this.serializedName;
	}
}
