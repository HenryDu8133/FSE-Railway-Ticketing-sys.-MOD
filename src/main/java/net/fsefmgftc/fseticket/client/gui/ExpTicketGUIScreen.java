package net.fsefmgftc.fseticket.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.GuiGraphics;
import net.fsefmgftc.fseticket.world.inventory.ExpTicketGUIMenu;
import net.fsefmgftc.fseticket.init.FseticketModScreens;
import net.fsefmgftc.fseticket.util.QRCodeGenerator;
import com.mojang.blaze3d.systems.RenderSystem;

public class ExpTicketGUIScreen extends AbstractContainerScreen<ExpTicketGUIMenu>
		implements FseticketModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private ImageButton imagebutton_localticket;
	private ResourceLocation qrTexture = null;
	private static final Style JF = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath("fonts","jnr"));
	private static final Style CF = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath("fonts","misans_demibold"));
	private static final ResourceLocation IMG0 = ResourceLocation.parse("fseticket:textures/screens/che_piao_bei_jing2.png");

	public ExpTicketGUIScreen(ExpTicketGUIMenu c, Inventory inv, Component text) {
		super(c, inv, text);
		this.world = c.world; this.x = c.x; this.y = c.y; this.z = c.z;
		this.entity = c.entity; this.imageWidth = 0; this.imageHeight = 0;
	}
	@Override public void updateMenuState(int t, String n, Object s) {}
	@Override public void render(GuiGraphics g, int mx, int my, float pt) { super.render(g, mx, my, pt); }
	@Override public boolean keyPressed(int k, int b, int c) {
		if (k == 256) { minecraft.player.closeContainer(); return true; } return super.keyPressed(k, b, c);
	}
	@Override protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
		RenderSystem.setShaderColor(1,1,1,1); RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
		g.blit(ResourceLocation.parse("fseticket:textures/screens/local_ticket_gui.png"), leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
		g.blit(IMG0, leftPos + -126, topPos + -75, 0, 0, 260, 163, 260, 163);
		if (qrTexture != null) g.blit(qrTexture, leftPos + 65, topPos + 13, 0, 0, 52, 52, 52, 52);
		RenderSystem.disableBlend();
	}
	private String nv(String s){return s==null||s.isEmpty()||s.equals("\"\"")?"---":s;}
	private Component LJF(String s){return Component.literal(s).withStyle(JF);}
	private Component TJF(String k){return Component.translatable(k).withStyle(JF);}
	private Component LCF(String s){return Component.literal(s).withStyle(CF);}
	private Component TCF(String k){return Component.translatable(k).withStyle(CF);}
		private String ud(String s) {
		if(s==null||s.isEmpty()||!s.contains("\\u"))return s;
		try{StringBuilder sb=new StringBuilder();int i=0;
		while(i<s.length()){if(i+5<s.length()&&s.charAt(i)=='\\'&&s.charAt(i+1)=='u'){sb.append((char)Integer.parseInt(s.substring(i+2,i+6),16));i+=6;}else{sb.append(s.charAt(i));i++;}}
		return sb.toString();}catch(Exception e){return s;}
	}
@Override protected void renderLabels(GuiGraphics g, int mx, int my) {
		ExpTicketGUIMenu m = (ExpTicketGUIMenu) menu;
		String from = nv(m.startNameEn), to = nv(m.terminalNameEn);
		String ss = nv(m.startStation), ts = nv(m.terminalStation);
		String fcnu = ud(nv(m.fromNameCnU)), tcnu = ud(nv(m.toNameCnU));
		String tid = nv(m.ticketId), odt = nv(m.orderDatetime);
		String rides = String.valueOf(m.rides), costStr = String.format("%.2f", m.cost);
		g.drawString(font, TJF("gui.fseticket.exp_ticket_gui.label_type"), -22, -59, -16777216, false);
		g.drawString(font, TJF("gui.fseticket.exp_ticket_gui.label_arrow"), -12, -46, -16777216, false);
		g.drawString(font, LJF(from), -108, -32, -16777216, false);
		g.drawString(font, LJF(to), 21, -31, -16777216, false);
		g.drawString(font, LJF(rides), -75, -4, -16777216, false);
		g.drawString(font, LJF(tid), 47, 75, -16777216, false);
		g.drawString(font, TJF("gui.fseticket.exp_ticket_gui.label_validtrip1"), -115, -4, -16777216, false);
		g.drawString(font, TJF("gui.fseticket.exp_ticket_gui.label_validtrip2"), -59, -4, -12829636, false);
		g.drawString(font, TJF("gui.fseticket.exp_ticket_gui.label_tip1"), -117, 54, -12829636, false);
		g.drawString(font, TJF("gui.fseticket.exp_ticket_gui.label_tip2"), -117, 67, -12829636, false);
		g.drawString(font, LJF(costStr), -108, 9, -12829636, false);
		g.drawString(font, LJF(odt), -115, 23, -12829636, false);
		g.drawString(font, LJF(ss), -108, -21, -6710887, false);
		g.drawString(font, LJF(ts), 22, -21, -6710887, false);
		g.drawString(font, TJF("gui.fseticket.exp_ticket_gui.label_title"), -27, -70, -12829636, false);
		g.drawString(font, LCF(fcnu), -108, -46, -12829636, false);
		g.drawString(font, LCF(tcnu), 20, -46, -12829636, false);
	}
	@Override public void init() {
		super.init();
		String tid = nv(((ExpTicketGUIMenu)menu).ticketId);
		if (!tid.equals("---")) qrTexture = QRCodeGenerator.getOrGenerate("https://ticket.fse-media.group/detail/" + tid, 64);
		imagebutton_localticket = new ImageButton(leftPos + 65, topPos + 13, 52, 52,
			new WidgetSprites(ResourceLocation.parse("fseticket:textures/screens/localticket.png"), ResourceLocation.parse("fseticket:textures/screens/localticket.png")),
			e -> { if (!tid.equals("---")) net.minecraft.Util.getPlatform().openUri("https://ticket.fse-media.group/detail/" + tid); })
			{ @Override public void renderWidget(GuiGraphics g, int mx, int my, float pt) {} };
		addRenderableWidget(imagebutton_localticket);
	}
}