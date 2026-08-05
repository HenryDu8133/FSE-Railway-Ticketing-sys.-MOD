package net.fsefmgftc.fseticket.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fsefmgftc.fseticket.FseticketMod;
import net.fsefmgftc.fseticket.init.FseticketModMenus;
import net.fsefmgftc.fseticket.util.QRCodeGenerator;
import net.fsefmgftc.fseticket.util.TicketDataUtil;
import net.fsefmgftc.fseticket.world.inventory.AbstractTicketMenu;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTicketScreen<T extends AbstractTicketMenu> extends AbstractContainerScreen<T>
	implements FseticketModMenus.ScreenAccessor {
	private static final ResourceLocation MAIN_TEXTURE = ResourceLocation.parse("fseticket:textures/screens/local_ticket_gui.png");
	private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.parse("fseticket:textures/screens/che_piao_bei_jing2.png");
	private static final ResourceLocation QR_BUTTON_TEXTURE = ResourceLocation.parse("fseticket:textures/screens/localticket.png");

	protected static final Style JF = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath(FseticketMod.MODID, "jnr"));
	protected static final Style CF = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath(FseticketMod.MODID, "misans_demibold"));
	protected static final Style NUMBER_FONT = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath(FseticketMod.MODID, "monoid_regular"));

	private ResourceLocation qrTexture;

	protected AbstractTicketScreen(T menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 0;
		this.imageHeight = 0;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object state) {
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256 && minecraft != null && minecraft.player != null) {
			minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		graphics.blit(MAIN_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
		graphics.blit(OVERLAY_TEXTURE, leftPos - 126, topPos - 75, 0, 0, 260, 163, 260, 163);
		if (qrTexture != null) {
			graphics.blit(qrTexture, leftPos + 65, topPos + 13, 0, 0, 52, 52, 52, 52);
		}
		RenderSystem.disableBlend();
	}

	@Override
	protected final void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		TicketScreenLayout layout = getLayout();
		String from = TicketDataUtil.normalizedDisplay(menu.startNameEn);
		String to = TicketDataUtil.normalizedDisplay(menu.terminalNameEn);
		String startStation = TicketDataUtil.normalizedDisplay(menu.startStation);
		String terminalStation = TicketDataUtil.normalizedDisplay(menu.terminalStation);
		String fromNameCn = TicketDataUtil.decodeUnicodeEscapes(TicketDataUtil.normalizedDisplay(menu.fromNameCnU));
		String toNameCn = TicketDataUtil.decodeUnicodeEscapes(TicketDataUtil.normalizedDisplay(menu.toNameCnU));
		String ticketId = TicketDataUtil.normalizedDisplay(menu.ticketId);
		String orderDatetime = TicketDataUtil.normalizedDisplay(menu.orderDatetime);
		String rides = String.valueOf(menu.rides);
		String cost = String.format("%.2f", menu.cost);

		graphics.drawString(font, styledTranslatable("label_type"), layout.typeLabelX(), -59, -16777216, false);
		graphics.drawString(font, styledTranslatable("label_arrow"), -12, -46, -16777216, false);
		graphics.drawString(font, styledLiteral(from, JF), -108, -32, -16777216, false);
		graphics.drawString(font, styledLiteral(to, JF), 21, -31, -16777216, false);
		graphics.drawString(font, styledLiteral(rides, JF), layout.ridesX(), -4, -16777216, false);
		graphics.drawString(font, styledLiteral(ticketId, NUMBER_FONT), 47, 75, -16777216, false);
		graphics.drawString(font, styledTranslatable("label_validtrip1"), -115, -4, -16777216, false);
		graphics.drawString(font, styledTranslatable("label_validtrip2"), -59, -4, -12829636, false);
		graphics.drawString(font, styledTranslatable("label_tip1"), -117, 54, -12829636, false);
		graphics.drawString(font, styledTranslatable("label_tip2"), -117, 67, -12829636, false);
		graphics.drawString(font, styledLiteral(cost, JF), -108, 9, -12829636, false);
		graphics.drawString(font, styledLiteral(orderDatetime, JF), -115, 23, -12829636, false);
		graphics.drawString(font, styledLiteral(startStation, JF), -108, -21, -6710887, false);
		graphics.drawString(font, styledLiteral(terminalStation, JF), 22, -21, -6710887, false);
		graphics.drawString(font, styledTranslatable("label_title"), layout.titleX(), -70, -12829636, false);
		graphics.drawString(font, styledLiteral(fromNameCn, CF), -108, -46, -12829636, false);
		graphics.drawString(font, styledLiteral(toNameCn, CF), 20, -46, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		String ticketId = TicketDataUtil.normalizedDisplay(menu.ticketId);
		if (!TicketDataUtil.DISPLAY_PLACEHOLDER.equals(ticketId)) {
			qrTexture = QRCodeGenerator.getOrGenerate(TicketDataUtil.TICKET_DETAIL_BASE_URL + ticketId, 64);
		}

		addRenderableWidget(new ImageButton(
			leftPos + 65,
			topPos + 13,
			52,
			52,
			new WidgetSprites(QR_BUTTON_TEXTURE, QR_BUTTON_TEXTURE),
			button -> openTicketDetail(ticketId)
		) {
			@Override
			public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			}
		});
	}

	private void openTicketDetail(String ticketId) {
		if (!TicketDataUtil.DISPLAY_PLACEHOLDER.equals(ticketId)) {
			Util.getPlatform().openUri(TicketDataUtil.TICKET_DETAIL_BASE_URL + ticketId);
		}
	}

	private Component styledLiteral(String value, Style style) {
		return Component.literal(value).withStyle(style);
	}

	private Component styledTranslatable(String keySuffix) {
		return Component.translatable(getTranslationPrefix() + keySuffix).withStyle(JF);
	}

	protected abstract String getTranslationPrefix();

	protected abstract TicketScreenLayout getLayout();

	protected record TicketScreenLayout(int typeLabelX, int ridesX, int titleX) {
	}
}
