# FixICGUI.ps1 - Generate ICGUIScreen from ICGUI.mod.json
$jsonFile = "D:\TaoYuan2023_HSTG\HSTG\MODCreate\fseticket\elements\ICGUI.mod.json"
$outFile = "D:\TaoYuan2023_HSTG\HSTG\MODCreate\fseticket\src\main\java\net\fsefmgftc\fseticket\client\gui\ICGUIScreen.java"
$raw = [System.IO.File]::ReadAllText($jsonFile, [System.Text.Encoding]::UTF8)

# Parse labels from JSON
$labels = @()
$lblR = '"name":\s*"([^"]+)".*?"value":\s*(-?\d+).*?"hasShadow":\s*(true|false).*?"x":\s*(-?\d+).*?"y":\s*(-?\d+)'
foreach ($m in [regex]::Matches($raw, $lblR, 'Singleline')) {
    $name = $m.Groups[1].Value; $color = $m.Groups[2].Value; $shadow = $m.Groups[3].Value
    $x = [int]$m.Groups[4].Value - 214; $y = [int]$m.Groups[5].Value - 120
    # Map to variable expression
    $val = switch -Wildcard ($name) {
        '*ownername*' { 'Component.literal(nv(m.ownerName)).withStyle(FONT)' }
        '*balance*'   { 'scaled' }
        '*cardid*'    { 'Component.literal(nv(m.cardId)).withStyle(FONT)' }
        default       { "Component.translatable(`"gui.fseticket.icgui.$name`").withStyle(FONT)" }
    }
    $labels += "`t`tif($val -eq "scaled"){ "g.pose().pushPose();g.pose().scale(1.3f,1.3f,1);g.drawString(font,Component.literal(String.format(\"%.2f\",m.balance)).withStyle(FONT),(int)($x/1.3),$y-5,$color,$shadow);g.pose().popPose();" } else { "g.drawString(font, $val, $x, $y, $color, $shadow);" }"
}
$labelBlock = ($labels -join "`n")

$code = @"
package net.fsefmgftc.fseticket.client.gui;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.fsefmgftc.fseticket.world.inventory.ICGUIMenu;
import net.fsefmgftc.fseticket.init.FseticketModScreens;
import net.fsefmgftc.fseticket.util.QRCodeGenerator;
import com.mojang.blaze3d.systems.RenderSystem;

public class ICGUIScreen extends AbstractContainerScreen<ICGUIMenu> implements FseticketModScreens.ScreenAccessor {
	private ResourceLocation qrTexture=null;
	private static final Style FONT=Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath("fonts","misans_demibold"));

	public ICGUIScreen(ICGUIMenu c,net.minecraft.world.entity.player.Inventory inv,Component text){super(c,inv,text);imageWidth=260;imageHeight=163;}
	@Override public void updateMenuState(int t,String n,Object s){}
	@Override public void render(GuiGraphics g,int mx,int my,float pt){super.render(g,mx,my,pt);}
	@Override public boolean keyPressed(int k,int b,int c){if(k==256){minecraft.player.closeContainer();return true;}return super.keyPressed(k,b,c);}
	@Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){
		RenderSystem.setShaderColor(1,1,1,1);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
		g.blit(ResourceLocation.parse("fseticket:textures/screens/fseic.png"),leftPos,topPos,0,0,260,163,260,163);
		if(qrTexture!=null)g.blit(qrTexture,leftPos+196,topPos+105,0,0,52,52,52,52);
		RenderSystem.disableBlend();
	}
	private String nv(String s){return s==null||s.isEmpty()||s.equals("\"\"")?"---":s;}
	@Override protected void renderLabels(GuiGraphics g,int mx,int my){
		ICGUIMenu m=(ICGUIMenu)menu;
$labelBlock
	}
	@Override public void init(){
		super.init();
		String cid=nv(((ICGUIMenu)menu).cardId);
		if(!cid.equals("---"))qrTexture=QRCodeGenerator.getOrGenerate("https://ticket.fse-media.group/ic/"+cid,64);
	}
}
"@

$utf8 = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($outFile, $code, $utf8)
Write-Host "Done! $($labels.Count) labels" -ForegroundColor Green
