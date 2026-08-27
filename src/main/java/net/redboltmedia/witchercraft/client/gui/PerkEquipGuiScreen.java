package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.PerkEquipGuiMenu;
import net.redboltmedia.witchercraft.network.PerkEquipGuiButtonMessage;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * Perk equip screen - slice 1a scaffold.
 *
 * HAND-MAINTAINED, locked_code=true: MCreator must not regenerate this file.
 * For now it only proves the plumbing - it draws the data-driven slot/socket
 * geometry (from {@link PerkEquipLayout}) as placeholder text labels, plus a
 * Back button and Esc-to-close. Dynamic glyph icons, held-perk selection,
 * valid-slot highlight, tooltips and click routing arrive in slice 1b.
 */
public class PerkEquipGuiScreen extends AbstractContainerScreen<PerkEquipGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private Button button_back;

	public PerkEquipGuiScreen(PerkEquipGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text, PerkEquipLayout.PANEL_W, PerkEquipLayout.PANEL_H);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
		// No custom background art yet (slice 1b). The vanilla dimmed backdrop
		// from super is enough to see the panel while we validate geometry.
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		guiGraphics.text(this.font, "PERK EQUIP (placeholder)", 8, 8, -1, false);
		guiGraphics.text(this.font, "MED", PerkEquipLayout.MEDALLION_X + 8, PerkEquipLayout.MEDALLION_Y + 16, -1, false);
		for (int i = 0; i < 12; i++) {
			guiGraphics.text(this.font, "S" + (i + 1), PerkEquipLayout.SLOT_X[i] + 4, PerkEquipLayout.SLOT_Y[i] + 8, -1, false);
		}
		for (int g = 0; g < 4; g++) {
			guiGraphics.text(this.font, "M" + (g + 1), PerkEquipLayout.SOCKET_X[g] + 4, PerkEquipLayout.SOCKET_Y[g] + 8, -1, false);
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = InputConstants.getKey(event).getValue();
		if (key == 256) { // Esc
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void init() {
		super.init();
		button_back = Button.builder(Component.literal("Back"), e -> {
			// Closing the container fires PerkEquipGuiMenu.removed() server-side,
			// which runs RecomputeEquippedPerks. The button packet is sent for
			// parity with the 1b action path (server handler no-ops on 0).
			ClientPacketDistributor.sendToServer(new PerkEquipGuiButtonMessage(0, x, y, z));
			this.minecraft.player.closeContainer();
		}).bounds(this.leftPos + PerkEquipLayout.PANEL_W - 50, this.topPos + PerkEquipLayout.PANEL_H - 24, 46, 20).build();
		this.addRenderableWidget(button_back);
	}
}
