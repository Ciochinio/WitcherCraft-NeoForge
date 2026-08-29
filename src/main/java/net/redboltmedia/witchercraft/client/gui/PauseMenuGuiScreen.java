package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.PauseMenuGuiMenu;
import net.redboltmedia.witchercraft.network.PauseMenuGuiButtonMessage;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;
import net.redboltmedia.witchercraft.client.gui.shell.WitcherGuiScreen;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.platform.InputConstants;

public class PauseMenuGuiScreen extends AbstractContainerScreen<PauseMenuGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_meditation;
	private Button button_character;
	private Button button_alchemy;
	private Button button_glossary;
	private Button button_bestiary;
	private Button button_skill_tree;

	public PauseMenuGuiScreen(PauseMenuGuiMenu container, Inventory inventory, Component text) {
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
	public boolean isPauseScreen() {
		return true;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
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
		// Meditation now lives in the WitcherCraft shell (key U) - open it directly,
		// client-side, same as the Skills button below. No server round-trip: opening
		// a screen is not server-authoritative state (the old container GUI + opener
		// were retired in slice 4).
		button_meditation = Button.builder(Component.translatable("gui.witchercraft.pause_menu_gui.button_meditation"), e -> {
			Minecraft.getInstance().setScreen(new WitcherGuiScreen("meditation"));
		}).bounds(this.leftPos + 216, this.topPos + 97, 77, 20).build();
		this.addRenderableWidget(button_meditation);
		button_character = Button.builder(Component.translatable("gui.witchercraft.pause_menu_gui.button_character"), e -> {
			int x = PauseMenuGuiScreen.this.x;
			int y = PauseMenuGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new PauseMenuGuiButtonMessage(1, x, y, z));
				PauseMenuGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}).bounds(this.leftPos + 51, this.topPos + 151, 77, 20).build();
		this.addRenderableWidget(button_character);
		button_alchemy = Button.builder(Component.translatable("gui.witchercraft.pause_menu_gui.button_alchemy"), e -> {
			int x = PauseMenuGuiScreen.this.x;
			int y = PauseMenuGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new PauseMenuGuiButtonMessage(2, x, y, z));
				PauseMenuGuiButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		}).bounds(this.leftPos + 51, this.topPos + 97, 76, 20).build();
		this.addRenderableWidget(button_alchemy);
		button_glossary = Button.builder(Component.translatable("gui.witchercraft.pause_menu_gui.button_glossary"), e -> {
			int x = PauseMenuGuiScreen.this.x;
			int y = PauseMenuGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new PauseMenuGuiButtonMessage(3, x, y, z));
				PauseMenuGuiButtonMessage.handleButtonAction(entity, 3, x, y, z);
			}
		}).bounds(this.leftPos + -114, this.topPos + 97, 77, 20).build();
		this.addRenderableWidget(button_glossary);
		button_bestiary = Button.builder(Component.translatable("gui.witchercraft.pause_menu_gui.button_bestiary"), e -> {
			int x = PauseMenuGuiScreen.this.x;
			int y = PauseMenuGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new PauseMenuGuiButtonMessage(4, x, y, z));
				PauseMenuGuiButtonMessage.handleButtonAction(entity, 4, x, y, z);
			}
		}).bounds(this.leftPos + -32, this.topPos + 97, 77, 20).build();
		this.addRenderableWidget(button_bestiary);
		// Skills now lives in the WitcherCraft shell (P) - open it directly,
		// client-side, same as WitcherGuiKeybind. No server round-trip: opening a
		// screen is not server-authoritative state, unlike the other pause-menu
		// buttons above, which still open their own MCreator container screens.
		button_skill_tree = Button.builder(Component.translatable("gui.witchercraft.pause_menu_gui.button_skill_tree"), e -> {
			Minecraft.getInstance().setScreen(new WitcherGuiScreen("skills"));
		}).bounds(this.leftPos + 133, this.topPos + 97, 77, 20).build();
		this.addRenderableWidget(button_skill_tree);
	}
}