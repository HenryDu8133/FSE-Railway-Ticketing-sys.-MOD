package net.fsefmgftc.fseticket.client.gui;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.fsefmgftc.fseticket.world.inventory.ICGUIMenu;
import net.fsefmgftc.fseticket.init.FseticketModMenus;
import net.fsefmgftc.fseticket.util.QRCodeGenerator;
import com.mojang.blaze3d.systems.RenderSystem;

public class ICGUIScreen extends AbstractContainerScreen<ICGUIMenu> implements FseticketModMenus.ScreenAccessor {
	private ResourceLocation qrTexture=null;
	private static final Style FONT=Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath("fonts","misans_demibold"));

	public ICGUIScreen(ICGUIMenu c,net.minecraft.world.entity.player.Inventory inv,Component text){super(c,inv,text);imageWidth=0;imageHeight=0;}
	@Override public void updateMenuState(int t,String n,Object s){}
	@Override public void render(GuiGraphics g,int mx,int my,float pt){super.render(g,mx,my,pt);}
	@Override public boolean keyPressed(int k,int b,int c){if(k==256){minecraft.player.closeContainer();return true;}return super.keyPressed(k,b,c);}
	@Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){
		RenderSystem.setShaderColor(1,1,1,1);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
		g.blit(ResourceLocation.parse("fseticket:textures/screens/fseic.png"),leftPos-131,topPos-85,0,0,260,163,260,163);
		if(qrTexture!=null)g.blit(qrTexture,leftPos+65,topPos+20,0,0,52,52,52,52);
		RenderSystem.disableBlend();
	}
	private String nv(String s){return s==null||s.isEmpty()||s.equals("\"\"")?"---":s;}
	@Override protected void renderLabels(GuiGraphics g,int mx,int my){
		ICGUIMenu m=(ICGUIMenu)menu;
		g.drawString(font,Component.translatable("gui.fseticket.icgui.label_title").withStyle(FONT),24,-77,-12829636,false);
		g.drawString(font,Component.literal(nv(m.ownerName)).withStyle(FONT),-12,-55,-12829636,false);
		g.pose().pushPose();g.pose().scale(2f,2f,1);g.drawString(font,Component.literal(String.format("%.2f",m.balance)).withStyle(FONT),14,-15,-16758869,false);g.pose().popPose();
		g.drawString(font,Component.literal(nv(m.cardId)).withStyle(FONT),4,-1,-12829636,false);
	}
	@Override public void init(){
		super.init();
		String cid=nv(((ICGUIMenu)menu).cardId);
		if(!cid.equals("---"))qrTexture=QRCodeGenerator.getOrGenerate("https://ticket.fse-media.group/ic/"+cid,64);
	}
}