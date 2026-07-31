package net.fsefmgftc.fseticket.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.PacketDistributor;

import net.fsefmgftc.fseticket.network.BroadcastHostRenameMessage;

public class BroadcastHostRenameScreen extends Screen {
    private final BlockPos pos;
    private final String initialName;
    private EditBox nameBox;

    public BroadcastHostRenameScreen(BlockPos pos, String initialName) {
        super(Component.literal("重命名广播主机"));
        this.pos = pos;
        this.initialName = initialName;
    }

    @Override
    protected void init() {
        super.init();
        this.nameBox = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 20, 200, 20, Component.literal("主机名称"));
        this.nameBox.setMaxLength(50);
        this.nameBox.setValue(this.initialName);
        this.addRenderableWidget(this.nameBox);

        this.addRenderableWidget(Button.builder(Component.literal("确定"), button -> {
            PacketDistributor.sendToServer(new BroadcastHostRenameMessage(this.pos, this.nameBox.getValue()));
            this.minecraft.setScreen(null);
        }).bounds(this.width / 2 - 50, this.height / 2 + 10, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
