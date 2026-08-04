package net.fsefmgftc.fseticket.util;

import io.netty.buffer.Unpooled;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;

public final class TicketDataUtil {
	public static final String EMPTY_SERIALIZED_STRING = "\"\"";
	public static final String DISPLAY_PLACEHOLDER = "---";
	public static final String TICKET_DETAIL_BASE_URL = "https://ticket.fse-media.group/detail/";

	public static final String CARD_ID = "cardId";
	public static final String OWNER_NAME = "ownerName";
	public static final String BALANCE = "balance";
	public static final String ENTERED = "entered";
	public static final String EXITED = "exited";
	public static final String ENTRY_STATION = "entry_station";

	public static final String TYPE = "type";
	public static final String TYPE_LOCAL = "local";
	public static final String TYPE_LIMITED_EXPRESS = "limited_express";
	public static final String TYPE_SINGLE = "single";
	public static final String TYPE_FSE_PASS = "fse_pass";
	public static final String LINE_NAME = "line_name";
	public static final String START_NAME_EN = "start_name_en";
	public static final String TERMINAL_NAME_EN = "terminal_name_en";
	public static final String START_STATION = "start_station";
	public static final String TERMINAL_STATION = "terminal_station";
	public static final String FROM_NAME_CNU = "fromNameCnU";
	public static final String TO_NAME_CNU = "toNameCnU";
	public static final String RIDES = "rides";
	public static final String TICKET_ID = "ticketId";
	public static final String TIMESTAMP = "timestamp";
	public static final String COST = "cost";
	public static final String ORDER_DATETIME = "order_datetime";

	private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private TicketDataUtil() {
	}

	public static CompoundTag createBaseTicketTag(String type) {
		CompoundTag tag = new CompoundTag();
		tag.putString(START_NAME_EN, "");
		tag.putString(TERMINAL_NAME_EN, "");
		tag.putString(START_STATION, "");
		tag.putString(TERMINAL_STATION, "");
		tag.putString(FROM_NAME_CNU, "");
		tag.putString(TO_NAME_CNU, "");
		tag.putString(TYPE, type);
		tag.putInt(RIDES, 1);
		tag.putBoolean(ENTERED, false);
		tag.putBoolean(EXITED, false);
		tag.putString(TICKET_ID, "");
		tag.putLong(TIMESTAMP, 0L);
		tag.putDouble(COST, 0D);
		tag.putString(ORDER_DATETIME, "");
		return tag;
	}

	public static CompoundTag createSingleTripTicketTag() {
		CompoundTag tag = createBaseTicketTag(TYPE_SINGLE);
		tag.putString(LINE_NAME, "");
		return tag;
	}

	public static CompoundTag createICCardTag() {
		CompoundTag tag = new CompoundTag();
		tag.putString(CARD_ID, "");
		tag.putString(OWNER_NAME, "");
		tag.putDouble(BALANCE, 0D);
		return tag;
	}

	public static FriendlyByteBuf createBuffer() {
		return new FriendlyByteBuf(Unpooled.buffer());
	}

	public static void writeTicketMenuData(FriendlyByteBuf buffer, BlockPos pos, InteractionHand hand, CompoundTag tag) {
		buffer.writeBlockPos(pos);
		buffer.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1);
		buffer.writeUtf(tag.getString(START_NAME_EN));
		buffer.writeUtf(tag.getString(TERMINAL_NAME_EN));
		buffer.writeUtf(tag.getString(START_STATION));
		buffer.writeUtf(tag.getString(TERMINAL_STATION));
		buffer.writeUtf(tag.getString(FROM_NAME_CNU));
		buffer.writeUtf(tag.getString(TO_NAME_CNU));
		buffer.writeInt(tag.getInt(RIDES));
		buffer.writeUtf(tag.getString(TICKET_ID));
		buffer.writeDouble(tag.getDouble(COST));
		buffer.writeUtf(tag.getString(ORDER_DATETIME));
	}

	public static void writeICCardMenuData(FriendlyByteBuf buffer, BlockPos pos, InteractionHand hand, CompoundTag tag) {
		buffer.writeBlockPos(pos);
		buffer.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1);
		buffer.writeUtf(tag.getString(CARD_ID));
		buffer.writeUtf(tag.getString(OWNER_NAME));
		buffer.writeDouble(tag.getDouble(BALANCE));
	}

	public static String normalizedDisplay(String value) {
		return hasMeaningfulValue(value) ? value : DISPLAY_PLACEHOLDER;
	}

	public static boolean hasMeaningfulValue(String value) {
		return value != null && !value.isEmpty() && !EMPTY_SERIALIZED_STRING.equals(value);
	}

	public static String decodeUnicodeEscapes(String value) {
		if (value == null || value.isEmpty() || !value.contains("\\u")) {
			return value;
		}

		try {
			StringBuilder decoded = new StringBuilder();
			int index = 0;
			while (index < value.length()) {
				if (index + 5 < value.length() && value.charAt(index) == '\\' && value.charAt(index + 1) == 'u') {
					decoded.append((char) Integer.parseInt(value.substring(index + 2, index + 6), 16));
					index += 6;
					continue;
				}
				decoded.append(value.charAt(index));
				index++;
			}
			return decoded.toString();
		} catch (Exception ignored) {
			return value;
		}
	}

	public static String generateTicketId() {
		return String.format(
			"%c%c-%08d",
			(char) (Math.random() * 26 + 65),
			(char) (Math.random() * 26 + 65),
			(int) (Math.random() * 100000000)
		);
	}

	public static String generateCardId() {
		return "IC-" + UUID.randomUUID().toString().substring(0, 8);
	}

	public static String currentOrderDateTime() {
		return LocalDateTime.now().format(ORDER_TIME_FORMATTER);
	}

	public static Map<String, Object> buildTicketInfo(CompoundTag tag, String passengerName) {
		Map<String, Object> info = new LinkedHashMap<>();
		info.put(START_NAME_EN, tag.getString(START_NAME_EN));
		info.put(TERMINAL_NAME_EN, tag.getString(TERMINAL_NAME_EN));
		info.put(START_STATION, tag.getString(START_STATION));
		info.put(TERMINAL_STATION, tag.getString(TERMINAL_STATION));
		info.put(FROM_NAME_CNU, tag.getString(FROM_NAME_CNU));
		info.put(TO_NAME_CNU, tag.getString(TO_NAME_CNU));
		info.put(TYPE, tag.getString(TYPE));
		info.put(RIDES, tag.getInt(RIDES));
		info.put(ENTERED, tag.getBoolean(ENTERED));
		info.put(EXITED, tag.getBoolean(EXITED));
		info.put(TICKET_ID, tag.getString(TICKET_ID));
		info.put(TIMESTAMP, tag.getLong(TIMESTAMP));
		info.put(COST, tag.getDouble(COST));
		info.put(ORDER_DATETIME, tag.getString(ORDER_DATETIME));
		info.put("passenger", passengerName);
		return info;
	}

	public static Map<String, Object> buildICCardInfo(CompoundTag tag, String passengerName) {
		Map<String, Object> info = new LinkedHashMap<>();
		info.put(CARD_ID, tag.getString(CARD_ID));
		info.put(OWNER_NAME, tag.getString(OWNER_NAME));
		info.put(BALANCE, tag.getDouble(BALANCE));
		info.put(ENTERED, tag.getBoolean(ENTERED));
		info.put(ENTRY_STATION, tag.getString(ENTRY_STATION));
		info.put("passenger", passengerName);
		return info;
	}
}
