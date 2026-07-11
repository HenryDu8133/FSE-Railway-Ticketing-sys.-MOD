package net.fsefmgftc.fseticket.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;

import net.fsefmgftc.fseticket.world.inventory.FSEPassGUIMenu;
import net.fsefmgftc.fseticket.init.FseticketModMenus;

import com.mojang.blaze3d.systems.RenderSystem;

public class FSEPassGUIScreen extends AbstractContainerScreen<FSEPassGUIMenu> implements FseticketModMenus.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private static final ResourceLocation BACKGROUND = ResourceLocation.parse("fseticket:textures/screens/fse_pass_gui.png");
	private static final ResourceLocation IMAGE_0 = ResourceLocation.parse("fseticket:textures/screens/fsepass1.png");

	public FSEPassGUIScreen(FSEPassGUIMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 0;
		this.imageHeight = 0;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		guiGraphics.blit(IMAGE_0, this.leftPos + -124, this.topPos + -73, 0, 0, 260, 163, 260, 163);
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, Component.translatable("gui.fseticket.fse_pass_gui.label_startdate"), -88, -2, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.fseticket.fse_pass_gui.label_enddate"), 39, -2, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.fseticket.fse_pass_gui.label_validitperiod"), 34, 34, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
	}
}