# FixGUI.ps1
$jsonFile = "D:\TaoYuan2023_HSTG\HSTG\MODCreate\fseticket\elements\ExpTicketGUI.mod.json"
$outFile = "D:\TaoYuan2023_HSTG\HSTG\MODCreate\fseticket\src\main\java\net\fsefmgftc\fseticket\client\gui\ExpTicketGUIScreen.java"
$texDir = "D:\TaoYuan2023_HSTG\HSTG\MODCreate\fseticket\src\main\resources\assets\fseticket\textures\screens"
Add-Type -AssemblyName System.Drawing
$raw = [System.IO.File]::ReadAllText($jsonFile, [System.Text.Encoding]::UTF8)

$labels = @()
$lblR = '"type":\s*"label".*?"name":\s*"([^"]+)".*?"value":\s*(-?\d+).*?"hasShadow":\s*(true|false).*?"x":\s*(-?\d+).*?"y":\s*(-?\d+)'
foreach ($m in [regex]::Matches($raw, $lblR, 'Singleline')) {
    $labels += @{ name=$m.Groups[1].Value; color=$m.Groups[2].Value; shadow=$m.Groups[3].Value
        x=[int]$m.Groups[4].Value-214; y=[int]$m.Groups[5].Value-120 }
}

$images = @()
$imgR = '"type":\s*"image".*?"image":\s*"([^"]+)".*?"use1Xscale":\s*(true|false).*?"x":\s*(-?\d+).*?"y":\s*(-?\d+)'
foreach ($m in [regex]::Matches($raw, $imgR, 'Singleline')) {
    $f=$m.Groups[1].Value; $w=64;$h=64; $p=Join-Path $texDir $f
    if(Test-Path $p){try{$b=[System.Drawing.Image]::FromFile($p);$w=$b.Width;$h=$b.Height;$b.Dispose()}catch{}}
    $images += @{ file=$f; x=[int]$m.Groups[3].Value-214; y=[int]$m.Groups[4].Value-120; w=$w; h=$h }
}

function Get-Val($name) {
    switch -Wildcard ($name) {
        '*variableticketfrom*' { return 'from' }
        '*variableticketto*'   { return 'to' }
        '*variableticketid*'   { return 'tid' }
        '*variableticketrides*' { return 'rides' }
        '*variablecost*'       { return 'costStr' }
        '*variableorder_datetime*' { return 'odt' }
        '*variablestartstation*'   { return 'ss' }
        '*variableterminalstation*' { return 'ts' }
        '*variablefromnamecnu*' { return 'fcnu' }
        '*variabletonamecnu*'   { return 'tcnu' }
        default { return '' }
    }
}

# Use CJK font for Chinese labels, JNR for others
function Is-CnLabel($name) {
    return $name -match 'fromnamecnu|tonamecnu'
}

$sb = New-Object System.Text.StringBuilder
foreach ($l in $labels) {
    $v = Get-Val $l.name
    $fnt = if (Is-CnLabel $l.name) { 'CF' } else { 'JF' }
    if ($v) {
        [void]$sb.AppendLine("`t`tg.drawString(font, L$fnt($v), $($l.x), $($l.y), $($l.color), $($l.shadow));")
    } else {
        [void]$sb.AppendLine("`t`tg.drawString(font, T$fnt(`"gui.fseticket.exp_ticket_gui.$($l.name)`"), $($l.x), $($l.y), $($l.color), $($l.shadow));")
    }
}
$labelBlock = $sb.ToString().TrimEnd()

$sb2 = New-Object System.Text.StringBuilder
$sb3 = New-Object System.Text.StringBuilder
for($i=0;$i -lt $images.Count;$i++){
    $img=$images[$i]
    [void]$sb2.AppendLine("`tprivate static final ResourceLocation IMG$i = ResourceLocation.parse(`"fseticket:textures/screens/$($img.file)`");")
    [void]$sb3.AppendLine("`t`tg.blit(IMG$i, leftPos + $($img.x), topPos + $($img.y), 0, 0, $($img.w), $($img.h), $($img.w), $($img.h));")
}
$imgFields = $sb2.ToString().TrimEnd()
$imgBlits = $sb3.ToString().TrimEnd()

$code = @"
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
$imgFields

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
$imgBlits
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
$labelBlock
	}
	@Override public void init() {
		super.init();
		String tid = nv(((ExpTicketGUIMenu)menu).ticketId);
		if (!tid.equals("---")) qrTexture = QRCodeGenerator.getOrGenerate("https://ticket-detail.fse-media.group/" + tid, 64);
		imagebutton_localticket = new ImageButton(leftPos + 65, topPos + 13, 52, 52,
			new WidgetSprites(ResourceLocation.parse("fseticket:textures/screens/localticket.png"), ResourceLocation.parse("fseticket:textures/screens/localticket.png")),
			e -> { if (!tid.equals("---")) net.minecraft.Util.getPlatform().openUri("https://ticket-detail.fse-media.group/" + tid); })
			{ @Override public void renderWidget(GuiGraphics g, int mx, int my, float pt) {} };
		addRenderableWidget(imagebutton_localticket);
	}
}
"@

$utf8 = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($outFile, $code, $utf8)
Write-Host "Done! labels=$($labels.Count) images=$($images.Count)" -ForegroundColor Green
