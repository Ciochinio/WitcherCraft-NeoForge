package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.SignGuiMenu;
import net.redboltmedia.witchercraft.network.SignGuiButtonMessage;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.platform.InputConstants;

public class SignGuiScreen extends AbstractContainerScreen<SignGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_yrden;
	private Button button_quen;
	private Button button_igni;
	private Button button_axii;
	private Button button_aard;
	private static final Identifier IMAGE_0 = Identifier.parse("witchercraft:textures/screens/axii.png");
	private static final Identifier IMAGE_1 = Identifier.parse("witchercraft:textures/screens/aard.png");
	private static final Identifier IMAGE_2 = Identifier.parse("witchercraft:textures/screens/quen.png");
	private static final Identifier IMAGE_3 = Identifier.parse("witchercraft:textures/screens/yrden.png");
	private static final Identifier IMAGE_4 = Identifier.parse("witchercraft:textures/screens/igni.png");

	public SignGuiScreen(SignGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text, 176, 166);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_0, this.leftPos + 112, this.topPos + -14, 0, 0, 64, 64, 64, 64);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_1, this.leftPos + 172, this.topPos + 23, 0, 0, 64, 64, 64, 64);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_2, this.leftPos + -9, this.topPos + -7, 0, 0, 64, 64, 64, 64);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_3, this.leftPos + -62, this.topPos + 31, 0, 0, 64, 64, 64, 64);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_4, this.leftPos + 52, this.topPos + -36, 0, 0, 64, 64, 64, 64);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = InputConstants.getKey(event).getValue();
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
		button_yrden = Button.builder(Component.translatable("gui.witchercraft.sign_gui.button_yrden"), e -> {
			int x = SignGuiScreen.this.x;
			int y = SignGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new SignGuiButtonMessage(0, x, y, z));
				SignGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + -57, this.topPos + 85, 51, 20).build();
		this.addRenderableWidget(button_yrden);
		button_quen = Button.builder(Component.translatable("gui.witchercraft.sign_gui.button_quen"), e -> {
			int x = SignGuiScreen.this.x;
			int y = SignGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new SignGuiButtonMessage(1, x, y, z));
				SignGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}).bounds(this.leftPos + 2, this.topPos + 50, 46, 20).build();
		this.addRenderableWidget(button_quen);
		button_igni = Button.builder(Component.translatable("gui.witchercraft.sign_gui.button_igni"), e -> {
			int x = SignGuiScreen.this.x;
			int y = SignGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new SignGuiButtonMessage(2, x, y, z));
				SignGuiButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		}).bounds(this.leftPos + 63, this.topPos + 23, 46, 20).build();
		this.addRenderableWidget(button_igni);
		button_axii = Button.builder(Component.translatable("gui.witchercraft.sign_gui.button_axii"), e -> {
			int x = SignGuiScreen.this.x;
			int y = SignGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new SignGuiButtonMessage(3, x, y, z));
				SignGuiButtonMessage.handleButtonAction(entity, 3, x, y, z);
			}
		}).bounds(this.leftPos + 123, this.topPos + 47, 46, 20).build();
		this.addRenderableWidget(button_axii);
		button_aard = Button.builder(Component.translatable("gui.witchercraft.sign_gui.button_aard"), e -> {
			int x = SignGuiScreen.this.x;
			int y = SignGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new SignGuiButtonMessage(4, x, y, z));
				SignGuiButtonMessage.handleButtonAction(entity, 4, x, y, z);
			}
		}).bounds(this.leftPos + 182, this.topPos + 81, 46, 20).build();
		this.addRenderableWidget(button_aard);
	}
}